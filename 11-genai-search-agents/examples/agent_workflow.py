"""Deterministic, allowlisted tool workflow."""

from collections.abc import Callable


def lookup_topic(topic: str) -> str:
    return {"transactions": "Transactions commit local database changes."}.get(
        topic.lower(), "No approved note found."
    )


TOOLS: dict[str, Callable[[str], str]] = {"lookup_topic": lookup_topic}


def run_agent(request: str, tool_name: str, argument: str) -> str:
    if tool_name not in TOOLS:
        raise ValueError(f"tool is not allowed: {tool_name}")
    result = TOOLS[tool_name](argument)
    return f"Request: {request}\nEvidence: {result}"


def main() -> None:
    print(run_agent("Explain transactions", "lookup_topic", "transactions"))
    try:
        run_agent("Delete data", "shell", "rm -rf")
    except ValueError as error:
        print(error)


if __name__ == "__main__":
    main()
