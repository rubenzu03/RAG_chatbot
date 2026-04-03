import requests
import os
import sys
import json
import re
import csv
from pathlib import Path
from dotenv import load_dotenv
from datetime import datetime, timezone
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
import torch
from bert_score import BERTScorer
from transformers import pipeline as hf_pipeline

def load_env_parent(env_name=".env"):
    parent = Path(__file__).resolve().parent.parent
    candidate = parent / env_name
    if candidate.exists():
        try:
            load_dotenv(dotenv_path=str(candidate), override=True)
        except Exception:
            pass


def resolve_path_parent(path_str, start_file=__file__):
    p = Path(path_str)
    if p.is_absolute() and p.exists():
        return str(p)
    if p.exists():
        return str(p)
    parent = Path(start_file).resolve().parent.parent
    candidate = parent / path_str
    if candidate.exists():
        return str(candidate)
    return path_str


load_env_parent()

API_URL = os.environ.get("RAG_API_URL", "http://localhost:8080/api/ai/ragquery")
DATASET = resolve_path_parent(os.environ.get("RAG_DATASET", "programming_dataset.json"))
TOKEN= resolve_path_parent(os.environ.get("BEARER_TOKEN"))
RESULTS_CSV = resolve_path_parent(os.environ.get("RAG_RESULTS_CSV", "results_temperature_0.csv"))
MODEL_NAME = resolve_path_parent(os.environ.get("MODEL_NAME"))
BENCH_CONVERSATION_PREFIX = os.environ.get("BENCH_CONVERSATION_PREFIX", "rag")
BENCH_ISOLATE_CONVERSATIONS = os.environ.get("BENCH_ISOLATE_CONVERSATIONS", "true").lower() in ("1", "true", "yes")

EMBEDDING_MODEL_NAME = os.environ.get("EMBEDDING_MODEL_NAME", "all-MiniLM-L6-v2")
NLI_MODEL_NAME = os.environ.get("NLI_MODEL", "facebook/bart-large-mnli")
BERT_SCORE_MODEL = os.environ.get("BERT_SCORE_MODEL", "distilroberta-base")
BERT_SCORE_DEVICE = os.environ.get("BERT_SCORE_DEVICE", "cpu").lower()
NLI_DEVICE = os.environ.get("NLI_DEVICE", "cpu").lower()

if NLI_DEVICE not in ("cpu", "cuda"):
    NLI_DEVICE = "cpu"
if BERT_SCORE_DEVICE not in ("cpu", "cuda"):
    BERT_SCORE_DEVICE = "cpu"

EMBEDDING_MODEL = SentenceTransformer(EMBEDDING_MODEL_NAME)

BERT_SCORER = None
BERT_SCORE_INIT_ERROR = None
print(f"[init] loading BERTScore model: {BERT_SCORE_MODEL} on {BERT_SCORE_DEVICE} ...")
try:
    BERT_SCORER = BERTScorer(
        model_type=BERT_SCORE_MODEL,
        lang="en",
        device=BERT_SCORE_DEVICE,
        rescale_with_baseline=False,
    )
    print("[init] BERTScore model ready")
except Exception as e:
    BERT_SCORE_INIT_ERROR = e
    BERT_SCORER = None
    print(f"[warn] BERTScore disabled: {e}")

NLI_PIPE = None
print(f"[init] loading NLI model: {NLI_MODEL_NAME} ...")
try:
    nli_device = 0 if (NLI_DEVICE == "cuda" and torch and torch.cuda.is_available()) else -1
    NLI_PIPE = hf_pipeline(
        "text-classification",
        model=NLI_MODEL_NAME,
        top_k=None,
        device=nli_device,
    )
    print(f"[init] NLI model ready on {'cuda' if nli_device == 0 else 'cpu'}")
except Exception as e:
    NLI_PIPE = None
    print(f"[warn] NLI disabled: {e}")

def load_dataset(path):
    with open(path, encoding="utf-8") as file:
        try:
            data = json.load(file)
        except json.JSONDecodeError:
            file.seek(0)
            items = []
            for line in file:
                line = line.strip()
                if not line:
                    continue
                try:
                    items.append(json.loads(line))
                except json.JSONDecodeError:
                    continue
            if items:
                return items
            raise

    if isinstance(data, list):
        return data
    if isinstance(data, dict):
        return [data]
    raise TypeError("Unsupported dataset format: expected object or array")


def sanitize_text(text):
    if not text:
        return ""
    text = re.sub(r"(?m)^\s*data:\s*", "", text)
    text = "".join(ch for ch in text if ch.isprintable() or ch in "\n\t")
    return re.sub(r"\s+", " ", text).strip()


def extract_sse_chunk_text(payload):
    if not payload:
        return ""

    try:
        obj = json.loads(payload)
    except Exception:
        return sanitize_text(payload)

    if isinstance(obj, str):
        return sanitize_text(obj)

    if isinstance(obj, dict):
        for key in ("response", "answer", "result", "text", "content", "message"):
            value = obj.get(key)
            if isinstance(value, str):
                return sanitize_text(value)

        choices = obj.get("choices")
        if isinstance(choices, list) and choices:
            first = choices[0]
            if isinstance(first, dict):
                if isinstance(first.get("text"), str):
                    return sanitize_text(first["text"])
                message = first.get("message")
                if isinstance(message, dict) and isinstance(message.get("content"), str):
                    return sanitize_text(message["content"])

    return ""

def embedding_similarity(answer: str, truth: str) -> float:
    emb_a = EMBEDDING_MODEL.encode([answer])
    emb_t = EMBEDDING_MODEL.encode([truth])
    return float(cosine_similarity(emb_a, emb_t)[0][0])


def bert_score(answer: str, truth: str) -> float | None:
    if not BERT_SCORER:
        return None
    try:
        _, _, f1 = BERT_SCORER.score([answer], [truth])
        value = float(f1[0].item() if hasattr(f1[0], "item") else f1[0])
        if BERT_SCORE_DEVICE == "cuda" and torch and torch.cuda.is_available():
            torch.cuda.empty_cache()
        return round(value, 4)
    except Exception as e:
        print(f"  [bert] error: {e}")
        return None


def _nli_one(premise: str, hypothesis: str) -> float:
    def _normalize_label(raw_label: str) -> str:
        label = (raw_label or "").upper().strip()
        if "ENTAIL" in label:
            return "ENTAILMENT"
        if "NEUTRAL" in label:
            return "NEUTRAL"
        if "CONTRADICT" in label:
            return "CONTRADICTION"
        if label.startswith("LABEL_"):
            try:
                idx = int(label.split("_", 1)[1])
                mapped = NLI_PIPE.model.config.id2label.get(idx, "")
                return _normalize_label(str(mapped))
            except Exception:
                return label
        return label

    max_chars = int(os.environ.get("NLI_MAX_CHARS", "900"))
    results = NLI_PIPE(
        f"{premise[:max_chars]} [SEP] {hypothesis[:max_chars]}",
        truncation=True,
    )

    if isinstance(results, list) and results and isinstance(results[0], list):
        results = results[0]
    if isinstance(results, dict):
        results = [results]

    scores = {}
    for row in results if isinstance(results, list) else []:
        if not isinstance(row, dict):
            continue
        label = _normalize_label(str(row.get("label", "")))
        score = float(row.get("score", 0.0) or 0.0)
        scores[label] = score

    if scores.get("CONTRADICTION", 0.0) > 0.5:
        return 0.0

    return scores.get("ENTAILMENT", 0.0) + scores.get("NEUTRAL", 0.0) * 0.35


def nli_judge(answer: str, truth: str) -> float | None:
    if not NLI_PIPE:
        return None
    try:
        score_a = _nli_one(answer, truth)
        score_b = _nli_one(truth, answer)

        score = (score_a + score_b) / 2.0
        if NLI_DEVICE == "cuda" and torch and torch.cuda.is_available():
            torch.cuda.empty_cache()
        return round(score, 4)
    except Exception as e:
        print(f"  [nli] error: {e}")
        return None


def compute_composite(
    embed_sim: float,
    bert: float | None,
    nli: float | None,
) -> float:
    w_nli = float(os.environ.get("W_NLI", 0.50))
    w_bert = float(os.environ.get("W_BERT", 0.25))
    w_embed = float(os.environ.get("W_EMBED", 0.25))

    parts, total_w = 0.0, 0.0

    if nli is not None:
        parts += w_nli * nli
        total_w += w_nli
    if bert is not None:
        parts += w_bert * bert
        total_w += w_bert

    parts += w_embed * embed_sim
    total_w += w_embed

    return round(parts / total_w, 4) if total_w else round(embed_sim, 4)

def main(limit=None):
    records = load_dataset(DATASET)
    if not NLI_PIPE:
        print("[warn] NLI unavailable — score_nli will be marked as n/a")
    if not BERT_SCORER:
        print(f"[warn] BERTScore init failed      ({BERT_SCORE_INIT_ERROR})")

    req_headers = {"Accept": "text/event-stream"}
    if TOKEN:
        req_headers["Authorization"] = f"Bearer {TOKEN.strip().strip(chr(34)).strip(chr(39))}"

    fieldnames = [
        "timestamp",
        "dataset",
        "record_index",
        "query",
        "response",
        "truth",
        "score_embed",
        "score_bert",
        "score_nli",
        "score_composite",
        "model",
    ]
    file_exists = os.path.exists(RESULTS_CSV)

    with open(RESULTS_CSV, "a", encoding="utf-8", newline="") as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        if not file_exists or os.path.getsize(RESULTS_CSV) == 0:
            writer.writeheader()

        for index, rec in enumerate(records, start=1):
            if limit and index > limit:
                break

            query = rec.get("query")
            truth = rec.get("answer")
            if not query:
                print(f"skip record {index}: no query")
                continue

            print(f"\n-- record {index}/{len(records)} --")
            collected = []
            conversation_id = f"{BENCH_CONVERSATION_PREFIX}-{index}" if BENCH_ISOLATE_CONVERSATIONS else BENCH_CONVERSATION_PREFIX
            try:
                resp = requests.post(
                    API_URL,
                    params={"query": query, "conversationId": conversation_id},
                    headers=req_headers,
                    timeout=(5, None),
                    stream=True,
                )
                resp.raise_for_status()

                for line in resp.iter_lines(decode_unicode=True):
                    if not line:
                        continue
                    text = line[len("data:"):].lstrip() if line.startswith("data:") else line
                    if not text or text == "[DONE]":
                        continue

                    chunk = extract_sse_chunk_text(text)
                    if chunk:
                        collected.append(chunk)
                    else:
                        collected.append(sanitize_text(text))

            except KeyboardInterrupt:
                print("interrupted")
                break
            except Exception as e:
                print(f"request failed record {index}: {e}")
                writer.writerow(
                    {
                        "timestamp": datetime.now(timezone.utc).isoformat(),
                        "dataset": DATASET,
                        "record_index": index,
                        "query": query,
                        "response": "",
                        "truth": truth or "",
                        "score_embed": "",
                        "score_bert": "",
                        "score_nli": "n/a",
                        "score_composite": "",
                        "model": MODEL_NAME,
                    }
                )
                continue

            response_text = extract_sse_chunk_text(" ".join(collected))

            if truth:
                s_embed = round(embedding_similarity(response_text, truth), 4)
                s_bert = bert_score(response_text, truth)
                s_nli = nli_judge(response_text, truth)
                s_comp = compute_composite(s_embed, s_bert, s_nli)

                print(
                    f"  embed={s_embed:.4f}"
                    f"  bert={s_bert if s_bert is not None else 'n/a'}"
                    f"  nli={s_nli if s_nli is not None else 'n/a'}"
                    f"  composite={s_comp}"
                )
            else:
                s_embed = s_bert = s_nli = s_comp = None

            writer.writerow(
                {
                    "timestamp": datetime.now(timezone.utc).isoformat(),
                    "dataset": DATASET,
                    "record_index": index,
                    "query": query,
                    "response": response_text,
                    "truth": truth or "",
                    "score_embed": f"{s_embed:.4f}" if s_embed is not None else "",
                    "score_bert": f"{s_bert:.4f}" if s_bert is not None else "",
                    "score_nli": f"{s_nli:.4f}" if s_nli is not None else "n/a",
                    "score_composite": f"{s_comp:.4f}" if s_comp is not None else "",
                    "model": MODEL_NAME,
                }
            )
            print(f"done record {index}/{len(records)}")

    print(f"\nresults -> {RESULTS_CSV}")
    return 0


if __name__ == "__main__":
    sys.exit(main())