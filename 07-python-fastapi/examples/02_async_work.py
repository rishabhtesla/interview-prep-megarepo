"""Lesson 3: independent asynchronous work.

Run:
    python3 examples/02_async_work.py
"""

from __future__ import annotations

import asyncio
import time


async def fetch(name: str, delay: float) -> str:
    await asyncio.sleep(delay)
    return f"{name} response"


async def main() -> None:
    started = time.perf_counter()
    results = await asyncio.gather(
        fetch("catalog", 0.1),
        fetch("pricing", 0.1),
    )
    elapsed = time.perf_counter() - started
    print(results)
    print(f"independent work completed in less than 0.2s: {elapsed < 0.2}")


if __name__ == "__main__":
    asyncio.run(main())
