import requests
import os
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity

API_URL = "http://localhost:8080/api/ai/simplequery"
DATASET = "programming_dataset.json"

EMBEDDING_MODEL = SentenceTransformer("all-MiniLM-L6-v2")