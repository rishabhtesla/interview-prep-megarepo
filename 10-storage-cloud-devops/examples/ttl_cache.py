"""A deterministic-shaped local TTL cache lesson."""

from dataclasses import dataclass
from time import monotonic


@dataclass
class Entry:
    value: str
    expires_at: float


class TtlCache:
    def __init__(self) -> None:
        self._entries: dict[str, Entry] = {}

    def put(self, key: str, value: str, ttl_seconds: float) -> None:
        self._entries[key] = Entry(value, monotonic() + ttl_seconds)

    def get(self, key: str) -> str | None:
        entry = self._entries.get(key)
        if entry is None:
            return None
        if entry.expires_at <= monotonic():
            del self._entries[key]
            return None
        return entry.value


def main() -> None:
    cache = TtlCache()
    print("miss:", cache.get("topic"))
    cache.put("topic", "React effects", ttl_seconds=1)
    print("hit:", cache.get("topic"))
    print("A cache hit is not proof that the database has this value.")


if __name__ == "__main__":
    main()
