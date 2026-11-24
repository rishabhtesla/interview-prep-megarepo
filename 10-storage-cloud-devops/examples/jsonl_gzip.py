"""Write and read compressed JSON Lines with only the Python standard library."""

import gzip
import json
from pathlib import Path
from tempfile import TemporaryDirectory


def main() -> None:
    records = [
        {"event_id": "e1", "kind": "created"},
        {"event_id": "e2", "kind": "paid"},
    ]
    with TemporaryDirectory() as directory:
        path = Path(directory) / "events.jsonl.gz"
        with gzip.open(path, "wt", encoding="utf-8") as output:
            for record in records:
                output.write(json.dumps(record, separators=(",", ":")) + "\n")

        with gzip.open(path, "rt", encoding="utf-8") as source:
            restored = [json.loads(line) for line in source if line.strip()]
        print("round trip:", restored)


if __name__ == "__main__":
    main()
