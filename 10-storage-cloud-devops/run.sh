#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
python3 examples/sql_lesson.py
python3 examples/ttl_cache.py
python3 examples/jsonl_gzip.py
kubectl apply --dry-run=client -f k8s/lesson.yaml >/dev/null 2>&1 || true
echo "Local storage lessons passed; Kubernetes validation is optional."
