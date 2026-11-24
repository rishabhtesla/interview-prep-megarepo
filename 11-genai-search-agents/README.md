# GenAI, semantic search, agents, MCP, and knowledge graphs

This track teaches the mechanics without requiring an API key. The local
examples use deterministic lexical vectors, explicit tool calls, and a small
graph. They explain the architecture, but they are not pretending that word
counts are a production embedding model.

## Setup

```bash
cd 11-genai-search-agents
./run.sh
```

Only the Python standard library is needed.

## Lesson 1 — Retrieval before generation

Source: [`examples/semantic_search.py`](examples/semantic_search.py).

The basic retrieval flow is:

```text
documents
-> normalize/chunk
-> represent as vectors
-> index
-> retrieve candidates
-> rerank
-> give selected context to an LLM
```

The local example uses TF-IDF-like sparse vectors and cosine similarity. A
production system may use an embedding model and vector database such as
pgvector, but the important design decisions remain:

- Chunk size and overlap.
- Metadata filters.
- Tenant/authorization boundary.
- Similarity threshold.
- Citation and source tracking.
- Freshness and re-indexing.
- What happens when no document is relevant.

Run:

```bash
python3 examples/semantic_search.py
```

The result includes the selected source IDs. A useful answer must not lose the
source boundary when context is passed to generation.

## Lesson 2 — Reranking

Initial retrieval optimizes recall: retrieve more candidates than you need.
Reranking applies a more expensive relevance score to a smaller candidate set.

```text
top-k vector retrieval -> candidate set -> reranker -> final context
```

The local example gives a small exact-term bonus after cosine similarity. A
production cross-encoder or model-based reranker needs evaluation data,
latency/cost limits, and protection against authorization bypass.

## Lesson 3 — Knowledge graphs

Source: [`examples/knowledge_graph.py`](examples/knowledge_graph.py).

A graph represents entities and relationships:

```text
service --publishes--> event
event   --consumed-by--> projection
```

Graph traversal can answer relationship questions that plain similarity search
misses. It also introduces identity resolution, stale edges, authorization,
cycles, and update consistency.

## Lesson 4 — Tool-using agents

Source: [`examples/agent_workflow.py`](examples/agent_workflow.py).

An agent is not merely “a prompt.” A safe workflow makes the loop explicit:

```text
user request
-> classify
-> choose allowed tool
-> validate tool input
-> execute tool
-> observe result
-> decide whether to continue
-> produce answer with evidence
```

The example uses a deterministic planner and allowlisted tools. It refuses an
unknown tool rather than executing arbitrary Python or shell input.

Production controls:

- Tool allowlists and typed schemas.
- Authentication and per-user authorization.
- Timeouts, budgets, and maximum steps.
- Redaction of secrets and personal data.
- Human approval for destructive actions.
- Prompt-injection-resistant retrieval and output handling.
- Audit logs and replayable traces.

## Lesson 5 — MCP

Source: [`examples/mcp_server.py`](examples/mcp_server.py).

The example demonstrates the shape of a JSON-RPC tool server:

```text
initialize
tools/list
tools/call
```

It is a teaching transport, not a claim that the official MCP SDK or every host
integration is present. A real MCP implementation must follow the protocol
version and transport selected by the host, validate arguments, and enforce
authorization outside the model.

Run:

```bash
printf '%s\n' \
  '{"jsonrpc":"2.0","id":1,"method":"tools/list"}' \
  '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"lookup_topic","arguments":{"topic":"transactions"}}}' \
  | python3 examples/mcp_server.py
```

## Lesson 6 — pgvector boundary

A vector database stores an embedding and metadata. The SQL boundary should
still enforce tenant and authorization filters:

```sql
SELECT document_id, content
FROM documents
WHERE tenant_id = :tenant_id
ORDER BY embedding <=> :query_embedding
LIMIT 5;
```

Similarity is not authorization. Apply access filters before returning context.

## Interview questions

1. What is the difference between retrieval and generation?
2. Why can a high-similarity document still be irrelevant?
3. How do you evaluate semantic search?
4. What is reranking and why is it more expensive?
5. How do you prevent a tenant from retrieving another tenant's data?
6. What is an agent tool boundary?
7. How do you limit an agent loop?
8. What is MCP responsible for, and what remains application responsibility?
9. How do knowledge graphs complement vector search?
10. How do you detect prompt injection and unsupported answers?

## Revision card

```text
retrieval supplies evidence
reranking improves candidate order
generation must retain source boundaries
similarity is not authorization
agent tools require typed allowlists and budgets
MCP transports tool capability; the application still owns security
local lexical search explains mechanics, not production model quality
```
