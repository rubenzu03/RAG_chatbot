import json
import re
import random

try:
    with open("output.json", "r") as f:
        data = json.load(f)
except Exception as e:
    print(f"Error loading JSON: {e}")
    exit(1)

filtered_data = []

# Exclude any query that contains these instruction-like phrases
instruction_phrases = r"\b(write|generate|create|list|summarize|explain|provide|give|show|describe|translate|make|tell|draft|can you|could you|would you|please)\b"

for item in data:
    if "Question" not in item or "Answer" not in item:
        continue
    
    if item["Question"] is None or item["Answer"] is None:
        continue

    q = item["Question"].strip()
    a = item["Answer"].strip()
    
    if not q or not a:
        continue
        
    q_lower = q.lower()
    
    # Strictly require the question to start with a Wh- question word or How
    allowed_starts = ("what ", "which ", "where ", "who ", "when ", "why ", "how ", "that ")
    
    if q_lower.startswith(allowed_starts) and q.endswith("?"):
        # Double check it doesn't contain instruction phrases
        if not re.search(instruction_phrases, q_lower):
            filtered_data.append({"query": q, "answer": a})

print(f"Total valid strict questions found: {len(filtered_data)}")

# Shuffle the questions
random.seed(42) # Reeds to use import random
random.shuffle(filtered_data)

filtered_data = filtered_data[:500]

print(f"Selected {len(filtered_data)} questions after shuffling.")

with open("output_500.json", "w") as f:
    json.dump(filtered_data, f, indent=4)
print("Saved to output_500.json")