"""Teaching-only JSON-RPC tool server with an MCP-shaped surface."""

from __future__ import annotations

import json
import sys


def response(request_id: int, result: object) -> str:
    return json.dumps({"jsonrpc": "2.0", "id": request_id, "result": result})


def handle(request: dict[str, object]) -> str:
    request_id = int(request["id"])
    method = request.get("method")
    if method == "tools/list":
        return response(request_id, {
            "tools": [{
                "name": "lookup_topic",
                "description": "Return an approved study note.",
                "inputSchema": {"type": "object", "required": ["topic"]},
            }]
        })
    if method == "tools/call":
        params = request.get("params")
        if not isinstance(params, dict) or params.get("name") != "lookup_topic":
            raise ValueError("unknown or malformed tool call")
        arguments = params.get("arguments")
        if not isinstance(arguments, dict) or not isinstance(arguments.get("topic"), str):
            raise ValueError("topic argument must be a string")
        return response(request_id, {
            "content": [{"type": "text", "text": f"Approved topic: {arguments['topic']}"}]
        })
    raise ValueError(f"unsupported method: {method}")


def main() -> None:
    for line in sys.stdin:
        try:
            print(handle(json.loads(line)), flush=True)
        except (ValueError, KeyError, json.JSONDecodeError) as error:
            print(json.dumps({
                "jsonrpc": "2.0",
                "id": None,
                "error": {"code": -32600, "message": str(error)},
            }), flush=True)


if __name__ == "__main__":
    main()
