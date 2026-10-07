import java.util.*;

/**
 * LC 133 (Medium): deep-copy the component reachable from a graph node.
 * https://leetcode.com/problems/clone-graph/
 *
 * Contract: finite graph, non-null neighbor entries; null start -> null.
 * Supports repeated labels, self-loops, cycles, parallel/directed edges.
 * Source is unchanged; copies preserve neighbor order and share no source nodes.
 * Pattern: identity memoization + BFS. Allocate a copy before exploring its edges,
 * so cycles find an existing copy rather than recursively cloning forever.
 * Map invariant: one copy per discovered source identity. Every processed edge
 * connects the corresponding copies, preserving both adjacency and sharing.
 * O(V+E) time, O(V) auxiliary map/queue, O(V+E) output, O(1) call stack.
 * A label-keyed map would incorrectly collapse distinct same-label nodes.
 */
public class CloneGraph {
    public GraphNode solve(GraphNode start) {
        if (start == null) return null;
        Map<GraphNode, GraphNode> copies = new IdentityHashMap<>();
        Deque<GraphNode> queue = new ArrayDeque<>();
        copies.put(start, new GraphNode(start.value));
        queue.add(start);
        while (!queue.isEmpty()) {
            GraphNode node = queue.remove();
            for (GraphNode neighbor : node.neighbors) {
                Objects.requireNonNull(neighbor, "null neighbor");
                if (!copies.containsKey(neighbor)) {
                    copies.put(neighbor, new GraphNode(neighbor.value));
                    queue.add(neighbor);
                }
                copies.get(node).neighbors.add(copies.get(neighbor));
            }
        }
        return copies.get(start);
    }
}
