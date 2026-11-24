#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
python3 -m unittest discover -s tests -v
python3 examples/01_basics.py
python3 examples/02_async_work.py
