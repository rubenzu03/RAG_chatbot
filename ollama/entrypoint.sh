#!/bin/bash
set -e

OLLAMA_START_TIMEOUT_SECONDS="${OLLAMA_START_TIMEOUT_SECONDS:-120}"
OLLAMA_PULL_TIMEOUT_SECONDS="${OLLAMA_PULL_TIMEOUT_SECONDS:-600}"

wait_for_ollama() {
  local timeout_seconds="$1"
  local elapsed=0

  while [ "$elapsed" -lt "$timeout_seconds" ]; do
    if /bin/ollama list >/dev/null 2>&1; then
      return 0
    fi

    sleep 1
    elapsed=$((elapsed + 1))
  done

  return 1
}

run_with_timeout() {
  local timeout_seconds="$1"
  shift

  "$@" &
  local command_pid=$!
  local elapsed=0

  while kill -0 "$command_pid" >/dev/null 2>&1; do
    if [ "$elapsed" -ge "$timeout_seconds" ]; then
      kill "$command_pid" >/dev/null 2>&1 || true
      wait "$command_pid" >/dev/null 2>&1 || true
      return 1
    fi

    sleep 1
    elapsed=$((elapsed + 1))
  done

  wait "$command_pid"
}

/bin/ollama serve &
OLLAMA_PID=$!

cleanup() {
  kill "$OLLAMA_PID" >/dev/null 2>&1 || true
}

trap cleanup INT TERM

echo "Waiting for Ollama to become ready..."
if ! wait_for_ollama "$OLLAMA_START_TIMEOUT_SECONDS"; then
  echo "Ollama did not become ready within ${OLLAMA_START_TIMEOUT_SECONDS}s"
  exit 1
fi

if [ -n "$OLLAMA_MODELS" ]; then
  echo "Pulling Ollama models: $OLLAMA_MODELS"
  for model in $OLLAMA_MODELS; do
    echo "Pulling model: $model"
    if ! run_with_timeout "$OLLAMA_PULL_TIMEOUT_SECONDS" /bin/ollama pull "$model"; then
      echo "Model pull timed out after ${OLLAMA_PULL_TIMEOUT_SECONDS}s: $model"
      exit 1
    fi
  done
  echo "All models pulled successfully!"
else
  echo "No models specified in OLLAMA_MODELS. Skipping model pull."
fi

wait "$OLLAMA_PID"
