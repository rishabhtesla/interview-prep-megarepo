"""Deterministic lexical retrieval lesson; no model or API key required."""

from __future__ import annotations

import math
import re
from collections import Counter
from dataclasses import dataclass


@dataclass(frozen=True)
class Document:
    document_id: str
    text: str


def terms(text: str) -> list[str]:
    return re.findall(r"[a-z0-9]+", text.lower())


def vector(text: str) -> Counter[str]:
    return Counter(terms(text))


def cosine(left: Counter[str], right: Counter[str]) -> float:
    shared = set(left) & set(right)
    numerator = sum(left[word] * right[word] for word in shared)
    left_norm = math.sqrt(sum(value * value for value in left.values()))
    right_norm = math.sqrt(sum(value * value for value in right.values()))
    return numerator / (left_norm * right_norm) if left_norm and right_norm else 0.0


def search(documents: list[Document], query: str, limit: int = 2) -> list[tuple[float, Document]]:
    query_vector = vector(query)
    ranked = [(cosine(query_vector, vector(document.text)), document) for document in documents]
    return sorted(ranked, key=lambda item: (-item[0], item[1].document_id))[:limit]


def main() -> None:
    documents = [
        Document("spring-tx", "A transaction commits local database changes."),
        Document("react-effects", "An effect synchronizes React with an external system."),
        Document("kafka-retry", "A consumer handles duplicate events with idempotency."),
    ]
    for score, document in search(documents, "database transaction"):
        print(f"{score:.3f} {document.document_id}: {document.text}")


if __name__ == "__main__":
    main()
