import requests
import os
import sys
import json
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity


API_URL = os.environ.get("RAG_API_URL", "http://localhost:8080/api/ai/ragquery")
DATASET = os.environ.get("RAG_DATASET", "programming_dataset.json")
TOKEN = os.environ.get("BEARER_TOKEN")

EMBEDDING_MODEL = SentenceTransformer("all-MiniLM-L6-v2")

def load_query(path):
	with open(path, encoding="utf-8") as f:
		data = json.load(f)
	return data.get("query")

def headers_from_env():
	headers = {"Accept": "text/event-stream"}
	if TOKEN:
		token = TOKEN.strip().strip('"').strip("'")
		headers["Authorization"] = f"Bearer {token}"
	return headers

def main():
	q = load_query(DATASET)
	if not q:
		print("No query found in dataset", file=sys.stderr)
		return 1

	headers = headers_from_env()
	try:
		resp = requests.post(API_URL, params={"query": q}, headers=headers, timeout=(5, None), stream=True)
		resp.raise_for_status()
		for line in resp.iter_lines(decode_unicode=True):
			if line:
				print(line)
	except KeyboardInterrupt:
		print("\nInterrupted by user")
	except Exception as e:
		print("Request failed:", e, file=sys.stderr)
		return 1
	return 0


if __name__ == "__main__":
	sys.exit(main())

