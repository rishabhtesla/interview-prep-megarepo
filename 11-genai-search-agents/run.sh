#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
python3 examples/semantic_search.py
python3 examples/knowledge_graph.py
python3 examples/agent_workflow.py
printf '%s\n' '{"jsonrpc":"2.0","id":1,"method":"tools/list"}' \
  | python3 examples/mcp_server.py
