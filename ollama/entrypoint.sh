#!/bin/bash

/bin/ollama serve &
OLLAMA_PID=$!

if [ -n "$OLLAMA_MODELS" ]; then
  echo "Pulling Ollama models: $OLLAMA_MODELS"
  for model in $OLLAMA_MODELS; do
    echo "Pulling model: $model"
    /bin/ollama pull "$model"
  done
  echo "All models pulled successfully!"
else
  echo "No models specified in OLLAMA_MODELS. Skipping model pull."
fi

wait $OLLAMA_PID
