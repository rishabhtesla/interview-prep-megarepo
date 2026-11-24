-- PostgreSQL + pgvector teaching boundary.
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE documents (
    document_id TEXT PRIMARY KEY,
    tenant_id TEXT NOT NULL,
    content TEXT NOT NULL,
    embedding vector(3) NOT NULL
);

CREATE INDEX documents_embedding_idx
    ON documents USING hnsw (embedding vector_cosine_ops);

-- Authorization filtering is part of the query, not an afterthought.
SELECT document_id, content
FROM documents
WHERE tenant_id = :tenant_id
ORDER BY embedding <=> :query_embedding
LIMIT 5;
