import requests
import os
import sys
import json
import re
import csv
from datetime import datetime, timezone
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity


API_URL = os.environ.get("RAG_API_URL", "http://localhost:8080/api/ai/ragquery")
DATASET = os.environ.get("RAG_DATASET", "programming_dataset.json")
TOKEN = os.environ.get("BEARER_TOKEN")
RESULTS_CSV = os.environ.get("RAG_RESULTS_CSV", "results.csv")

EMBEDDING_MODEL = SentenceTransformer("all-MiniLM-L6-v2")
MODEL_NAME = "granite4:350m"

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
	text = ''.join(ch for ch in text if ch.isprintable() or ch in '\n\t')
	return re.sub(r"\s+", " ", text).strip()


def main():
	records = load_dataset(DATASET)

	headers = {"Accept": "text/event-stream"}
	if TOKEN:
		token = TOKEN.strip().strip('"').strip("'")
		headers["Authorization"] = f"Bearer {token}"
	fieldnames = ["timestamp", "dataset", "record_index", "query", "response", "truth", "score", "model"]

	with open(RESULTS_CSV, "a", encoding="utf-8", newline="") as csvfile:
		writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
		if os.path.getsize(RESULTS_CSV) == 0:
			writer.writeheader()

		for index, rec in enumerate(records, start=1):
			query = rec.get("query")
			truth = rec.get("answer")
			if not query:
				print(f"skip record {index}: no query")
				continue

			print(f"start record {index}/{len(records)}")
			collected = []
			try:
				resp = requests.post(API_URL, params={"query": query}, headers=headers, timeout=(5, None), stream=True)
				resp.raise_for_status()
				for line in resp.iter_lines(decode_unicode=True):
					if not line:
						continue
					text = line
					if text.startswith("data:"):
						text = text[len("data:"):].lstrip()
					collected.append(text)
			except KeyboardInterrupt:
				print("interrupted")
				break
			except Exception as e:
				print(f"request failed record {index}: {e}")
				writer.writerow({
					"timestamp": datetime.now(timezone.utc).isoformat(),
					"dataset": DATASET,
					"record_index": index,
					"query": query,
					"response": "",
					"truth": truth or "",
					"score": "",
					"model": MODEL_NAME,
				})
				continue

			response_text = sanitize_text(" ".join(collected))
			score = similarity(response_text, truth) if truth else None
			if score is not None:
				print(f"record {index} score: {score:.4f}")

			writer.writerow({
				"timestamp": datetime.now(timezone.utc).isoformat(),
				"dataset": DATASET,
				"record_index": index,
				"query": query,
				"response": response_text,
				"truth": truth or "",
				"score": f"{score:.4f}" if score is not None else "",
				"model": MODEL_NAME,
			})
			print(f"done record {index}/{len(records)}")

	print(f"results -> {RESULTS_CSV}")
	return 0

def similarity(answer,truth):
	embed_answer = EMBEDDING_MODEL.encode([answer])
	embed_truth = EMBEDDING_MODEL.encode([truth])

	score = cosine_similarity(embed_answer, embed_truth)[0][0]
	return score

if __name__ == "__main__":
	sys.exit(main())

