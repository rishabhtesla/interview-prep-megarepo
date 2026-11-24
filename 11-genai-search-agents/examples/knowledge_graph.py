"""Small directed graph traversal for relationship-oriented retrieval."""

from collections import defaultdict, deque


def shortest_path(graph: dict[str, list[str]], start: str, goal: str) -> list[str]:
    queue = deque([(start, [start])])
    visited = {start}
    while queue:
        node, path = queue.popleft()
        if node == goal:
            return path
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append((neighbor, path + [neighbor]))
    return []


def main() -> None:
    graph = defaultdict(list)
    graph["catalog-service"].append("topic-created")
    graph["topic-created"].append("study-index")
    print(shortest_path(graph, "catalog-service", "study-index"))


if __name__ == "__main__":
    main()
