import os
import sys
import json
import time
import csv
from datetime import datetime, timezone
import requests

from pathlib import Path
from dotenv import load_dotenv

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

# Default to Ollama local server generate endpoint
API_URL = os.environ.get("RAG_API_URL", "http://localhost:11434/api/generate")
DATASET = resolve_path_parent(os.environ.get("RAG_DATASET", "programming_dataset.json"))
RESULTS_CSV = resolve_path_parent(os.environ.get("TOKEN_RESULTS_CSV", "simple_token_results_ollama.csv"))
MODEL_NAME = os.environ.get("MODEL_NAME", "granite4:350m")
BENCH_CONVERSATION_PREFIX = os.environ.get("BENCH_CONVERSATION_PREFIX", "simple-tok")
BENCH_ISOLATE_CONVERSATIONS = os.environ.get("BENCH_ISOLATE_CONVERSATIONS", "true").lower() in ("1", "true", "yes")


def load_records(path):
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    return data if isinstance(data, list) else [data]


def extract_text_from_ollama_response(resp):
    if isinstance(resp, str):
        return resp
    if not isinstance(resp, dict):
        return str(resp)

    # common direct fields
    for key in ("text", "output", "message", "content"):
        if key in resp and isinstance(resp[key], str):
            return resp[key]

    # choices -> messages
    if "choices" in resp and isinstance(resp["choices"], list):
        parts = []
        for c in resp["choices"]:
            if isinstance(c, dict):
                for key in ("text", "message", "content"):
                    if key in c and isinstance(c[key], str):
                        parts.append(c[key])
                if "message" in c and isinstance(c["message"], dict):
                    msg = c["message"].get("content")
                    if isinstance(msg, str):
                        parts.append(msg)
            elif isinstance(c, str):
                parts.append(c)
        if parts:
            return " ".join(parts)

    # generated list
    if "generated" in resp and isinstance(resp["generated"], list):
        parts = []
        for g in resp["generated"]:
            if isinstance(g, dict):
                for key in ("text", "content"):
                    if key in g and isinstance(g[key], str):
                        parts.append(g[key])
            elif isinstance(g, str):
                parts.append(g)
        if parts:
            return " ".join(parts)

    return json.dumps(resp)


def main(limit=10):
    records = load_records(DATASET)

    headers = {"Accept": "application/json, text/plain", "Content-Type": "application/json"}

    fieldnames = ["timestamp", "dataset", "record_index", "tokens", "time_s", "tps", "model"]
    file_exists = os.path.exists(RESULTS_CSV)

    with open(RESULTS_CSV, "a", encoding="utf-8", newline="") as csvfile:
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        if not file_exists or os.path.getsize(RESULTS_CSV) == 0:
            writer.writeheader()

        stats = []
        for i, rec in enumerate(records[:limit], start=1):
            q = rec.get("query", "")
            conversation_id = f"{BENCH_CONVERSATION_PREFIX}-{i}" if BENCH_ISOLATE_CONVERSATIONS else BENCH_CONVERSATION_PREFIX
            print(f"start {i}/{min(limit, len(records))}")
            start = time.perf_counter()
            chunks = []
            try:
                payload = {"model": MODEL_NAME, "prompt": q}
                r = requests.post(API_URL, json=payload, headers=headers, timeout=500)
                resp_text = ""
                try:
                    data = r.json()
                    resp_text = extract_text_from_ollama_response(data) or ""
                    if not resp_text and isinstance(data, list):
                        parts = []
                        for item in data:
                            if isinstance(item, str):
                                parts.append(item)
                            elif isinstance(item, dict):
                                parts.append(extract_text_from_ollama_response(item))
                        resp_text = " ".join([p for p in parts if p])
                except Exception:
                    resp_text = r.text or ""

                chunks = [resp_text]
            except Exception as e:
                elapsed = time.perf_counter() - start
                print(f"request failed {i}: {e}")
                writer.writerow({
                    "timestamp": datetime.now(timezone.utc).isoformat(),
                    "dataset": DATASET,
                    "record_index": i,
                    "tokens": 0,
                    "time_s": f"{elapsed:.3f}",
                    "tps": "",
                    "model": MODEL_NAME,
                })
                stats.append((i, 0, elapsed))
                continue

            elapsed = time.perf_counter() - start
            resp = " ".join(chunks)
            tokens = len(resp.split()) if resp else 0
            tps = tokens / elapsed if elapsed > 0 else 0
            print(f"done {i}: tokens={tokens} time={elapsed:.3f}s tps={tps:.2f}")

            writer.writerow({
                "timestamp": datetime.now(timezone.utc).isoformat(),
                "dataset": DATASET,
                "record_index": i,
                "tokens": tokens,
                "time_s": f"{elapsed:.3f}",
                "tps": f"{tps:.2f}",
                "model": MODEL_NAME,
            })
            stats.append((i, tokens, elapsed))

        total_tokens = sum(t for _, t, _ in stats)
        total_time = sum(tt for _, _, tt in stats)
        avg = (total_tokens / total_time) if total_time else 0
        print(f"summary: records={len(stats)} total_tokens={total_tokens} total_time={total_time:.3f}s avg_tps={avg:.2f}")

    return 0


if __name__ == "__main__":
    sys.exit(main(10))
