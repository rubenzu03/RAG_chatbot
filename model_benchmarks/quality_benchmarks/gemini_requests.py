import os
import sys
import json
import csv
import requests
import time
from pathlib import Path
from dotenv import load_dotenv
from datetime import datetime, timezone

import torch
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity
from bert_score import BERTScorer
from transformers import pipeline as hf_pipeline

from google import genai

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

DATASET = resolve_path_parent(os.environ.get("RAG_DATASET", "programming_dataset.json"))
RESULTS_CSV = resolve_path_parent(os.environ.get("RAG_RESULTS_CSV", "results_gemini.csv"))
MODEL_NAME = "gemini-3.1-flash-lite-preview"
API_KEY = os.environ.get("GOOGLE_API_KEY")


GEMINI_CLIENT = genai.Client()

# OPCIONES MODELO
NLI_MODEL_NAME = os.environ.get("NLI_MODEL", "facebook/bart-large-mnli")
BERT_SCORE_MODEL = os.environ.get("BERT_SCORE_MODEL", "distilroberta-base")
BERT_SCORE_DEVICE = os.environ.get("BERT_SCORE_DEVICE", "cpu").lower()
NLI_DEVICE = os.environ.get("NLI_DEVICE", "cpu").lower()

if NLI_DEVICE not in ("cpu", "cuda"):
    NLI_DEVICE = "cpu"
if BERT_SCORE_DEVICE not in ("cpu", "cuda"):
    BERT_SCORE_DEVICE = "cpu"

EMBEDDING_MODEL = SentenceTransformer(os.environ.get("EMBEDDING_MODEL_NAME", "all-MiniLM-L6-v2"))

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


def embedding_similarity(answer: str, truth: str) -> float:
    emb_a = EMBEDDING_MODEL.encode([answer])
    emb_t = EMBEDDING_MODEL.encode([truth])
    return float(cosine_similarity(emb_a, emb_t)[0][0])


def bert_score(answer: str, truth: str) -> float | None:
    if not BERT_SCORER:
        return None
    try:
        _, _, F1 = BERT_SCORER.score([answer], [truth])
        value = float(F1[0].item() if hasattr(F1[0], "item") else F1[0])
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

    MAX_CHARS = int(os.environ.get("NLI_MAX_CHARS", "900"))
    results = NLI_PIPE(
        f"{premise[:MAX_CHARS]} [SEP] {hypothesis[:MAX_CHARS]}",
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

    parts  += w_embed * embed_sim 
    total_w += w_embed

    return round(parts / total_w, 4) if total_w else round(embed_sim, 4)

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


def extract_response_text(payload) -> str:
    if not payload:
        return ""

    if isinstance(payload, (dict,)):
        obj = payload
    else:
        try:
            obj = json.loads(str(payload))
        except Exception:
            obj = None

    if obj is None:
        return str(payload).strip()

    if isinstance(obj, str):
        return obj.strip()

    if isinstance(obj, dict):
        for key in ("output_text", "text", "content", "response", "answer", "candidates", "output"):
            val = obj.get(key)
            if isinstance(val, str):
                return val.strip()
            if isinstance(val, list) and val:
                first = val[0]
                if isinstance(first, str):
                    return first.strip()
                if isinstance(first, dict):
                    for k2 in ("content", "text"):
                        v2 = first.get(k2)
                        if isinstance(v2, str):
                            return v2.strip()

        out = obj.get("output")
        if isinstance(out, list) and out:
            first = out[0]
            if isinstance(first, dict):
                for k2 in ("content", "text"):
                    v2 = first.get(k2)
                    if isinstance(v2, str):
                        return v2.strip()

    return str(obj).strip()


def send_to_gemini(query: str) -> str:
    resp = GEMINI_CLIENT.models.generate_content(model=MODEL_NAME, contents=query)
    return resp.text


def main():
    records = load_dataset(DATASET)

    fieldnames = [
        "timestamp", "dataset", "record_index", "query", "response", "truth",
        "score_embed", "score_bert", "score_nli",
        "score_composite",
        "model",
    ]
    file_exists = os.path.exists(RESULTS_CSV)

    with open(RESULTS_CSV, "a", encoding="utf-8", newline="") as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        if not file_exists or os.path.getsize(RESULTS_CSV) == 0:
            writer.writeheader()

        for index, rec in enumerate(records, start=1):

            query = rec.get("query") or rec.get("question") or rec.get("prompt")
            truth = rec.get("answer") or rec.get("truth") or rec.get("expected")
            if not query:
                print(f"skip record {index}: no query")
                continue

            print(f"\n-- record {index}/{len(records)} --")
            interrupted = False
            skip_record = False
            response_text = ""

            def _is_503_exception(exc) -> bool:
                try:
                    code = getattr(exc, "status_code", None)
                    if code is None and hasattr(exc, "status"):
                        code = getattr(exc, "status")
                    if code is None and hasattr(exc, "response") and hasattr(exc.response, "status_code"):
                        code = getattr(exc.response, "status_code")
                    if code is not None:
                        try:
                            if int(code) == 503:
                                return True
                        except Exception:
                            pass
                except Exception:
                    pass
                msg = str(exc).lower()
                if "503" in msg or "service unavailable" in msg:
                    return True
                return False

            while True:
                try:
                    response_text = send_to_gemini(query)
                    response_text = str(response_text).strip()
                    low = response_text.lower()
                    if "503" in response_text or "service unavailable" in low:
                        print(f"received 503-like response for record {index}, retrying after 30s")
                        time.sleep(30)
                        continue
                    break
                except KeyboardInterrupt:
                    print("interrupted")
                    interrupted = True
                    break
                except Exception as e:
                    if _is_503_exception(e):
                        print(f"received 503 for record {index}, retrying after 30s: {e}")
                        time.sleep(30)
                        continue
                    print(f"request failed record {index}: {e}")
                    writer.writerow({
                        "timestamp": datetime.now(timezone.utc).isoformat(),
                        "dataset": DATASET, "record_index": index,
                        "query": query, "response": "", "truth": truth or "",
                        "model": MODEL_NAME,
                    })
                    skip_record = True
                    break

            if interrupted:
                break

            if skip_record:
                try:
                    time.sleep(30)
                except KeyboardInterrupt:
                    print("sleep interrupted")
                    raise
                continue

            print(f"response preview: {response_text[:200]}")

            if truth:
                s_embed = round(embedding_similarity(response_text, truth), 4)
                s_bert  = bert_score(response_text, truth)
                s_nli   = nli_judge(response_text, truth)
                s_comp  = compute_composite(s_embed, s_bert, s_nli)

                print(
                    f"embed={s_embed:.4f}"
                    f"bert={s_bert if s_bert is not None else 'n/a'}"
                    f"nli={s_nli if s_nli is not None else 'n/a'}"
                    f"composite={s_comp}"
                )
            else:
                s_embed = s_bert = s_nli = s_comp = None

            writer.writerow({
                "timestamp": datetime.now(timezone.utc).isoformat(),
                "dataset": DATASET,
                "record_index": index,
                "query":query,
                "response":response_text,
                "truth": truth or "",
                "score_embed": f"{s_embed:.4f}" if s_embed is not None else "",
                "score_bert": f"{s_bert:.4f}"  if s_bert  is not None else "",
                "score_nli":f"{s_nli:.4f}"   if s_nli   is not None else "n/a",
                "score_composite": f"{s_comp:.4f}"  if s_comp  is not None else "",
                "model": MODEL_NAME,
            })
            print(f"done record {index}/{len(records)}")

            try:
                time.sleep(10)
            except KeyboardInterrupt:
                print("sleep interrupted")
                raise

    print(f"\nresults -> {RESULTS_CSV}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
