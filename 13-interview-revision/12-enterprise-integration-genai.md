# Enterprise integration, search, agents, MCP, and knowledge graphs

## Enterprise integration flow

```text
partner message
  -> parse hardened boundary
  -> validate contract
  -> normalize internal model
  -> route/transform
  -> timeout/retry/error channel
  -> downstream system
```

## Run enterprise examples

```bash
cd 12-enterprise-integration
./run.sh
mvn test
```

Read `CamelRouteLesson.java`, `SoapEnvelopeLesson.java`, and
`ServiceDiscoveryLesson.java`. The route uses local `direct:` and `mock:`
endpoints; the discovery sample is an in-memory model, not a real Eureka
server.

## Enterprise interview questions

### What is an Enterprise Integration Pattern?

**Answer:** It is a named, reusable message-flow solution such as content
filter, router, translator, splitter, aggregator, retry, or dead-letter
channel. Camel makes the route explicit; production code must still define
timeouts, retries, ordering, idempotency, and error ownership.

### Why harden SOAP/XML parsing?

**Answer:** Partner XML is untrusted input. Namespace-aware parsing and
disabled external entity/DTD resolution prevent ambiguous matching and XXE
network/file access. Validate the expected operation/schema and reject
malformed or unexpected input; do not return empty success.

### What does Eureka-style discovery solve?

**Answer:** It maps a logical service name to registered instances and can
support client-side selection/health metadata. It does not prove the instance
is healthy at request time, authenticate the caller, or replace timeouts,
circuit breakers, and telemetry.

## Search and GenAI flow

```text
documents
  -> chunk + metadata/authorization
  -> embedding/index
  -> retrieve candidates
  -> rerank
  -> selected context + citations
  -> generation
  -> grounded answer or explicit no-answer
```

## Run local search/agent examples

```bash
cd 11-genai-search-agents
./run.sh
python3 examples/semantic_search.py
printf '%s\n' \
  '{"jsonrpc":"2.0","id":1,"method":"tools/list"}' \
  '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"lookup_topic","arguments":{"topic":"transactions"}}}' \
  | python3 examples/mcp_server.py
```

The local vector implementation is deterministic lexical similarity, not a
production embedding model. The agent uses allowlisted deterministic tools,
not arbitrary shell execution.

## GenAI interview questions

### Retrieval versus generation?

**Answer:** Retrieval selects evidence from an indexed corpus; generation
produces language conditioned on the prompt/context. Retrieval quality,
authorization, freshness, citation, and no-answer behavior must be evaluated
separately from model fluency.

### Why rerank?

**Answer:** First-stage retrieval optimizes recall cheaply. A reranker applies a
more expensive relevance model to a smaller candidate set to improve ordering.
It adds latency/cost and needs labeled evaluation data; it must not bypass
tenant/authorization filters.

### How do you secure RAG?

**Answer:** Apply tenant and document authorization filters before returning
context, retain source IDs/citations, redact secrets, limit prompt/context
budgets, detect prompt injection in retrieved content, and refuse unsupported
answers. Similarity is not authorization.

```sql
SELECT document_id, content
FROM documents
WHERE tenant_id = :tenant_id
  AND can_read = true
ORDER BY embedding <=> :query_embedding
LIMIT 5;
```

### What is a safe agent loop?

**Answer:** Classify the request, select an allowlisted typed tool, validate
arguments, enforce identity/authorization, execute with timeout/budget, inspect
the result, and stop at a maximum number of steps. Destructive actions need
human approval or an explicit policy boundary.

### What does MCP provide?

**Answer:** MCP standardizes a host/model-facing capability boundary such as
initialization, tool listing, and tool calls. The application still owns
authentication, authorization, input validation, sandboxing, audit, budgets,
and secret handling. A protocol does not make a tool safe automatically.

## Configuration and failure exercise

Production retrieval configuration must specify:

```text
chunk size/overlap
embedding model/version
metadata and tenant filters
top-k and similarity threshold
reranker budget/timeout
index freshness/rebuild strategy
citation/no-answer policy
```

Change `semantic_search.py` so a query matches a document from another tenant.
Add the tenant filter before retrieval and a test proving the document is not
returned. Then send an unknown tool to `mcp_server.py`; it must reject the call,
not execute arbitrary input.

## Revision card

```text
Retrieval supplies evidence; generation supplies language.
Reranking improves candidate order at extra cost/latency.
Authorization filters precede context construction.
Sources/citations and no-answer behavior protect trust.
Agents need typed allowlists, budgets, timeouts, and audit.
MCP transports capability; application code owns security.
```
