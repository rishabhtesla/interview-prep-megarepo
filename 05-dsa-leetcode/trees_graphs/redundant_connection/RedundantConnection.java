import java.util.Objects;

/**
 * LC 684 (Medium): identify the edge that closes a cycle in an undirected graph.
 * https://leetcode.com/problems/redundant-connection/
 *
 * Contract: non-null edge list of m pairs, labels 1..m (validated). Empty -> empty.
 * Returns a COPY of the first edge whose endpoints were already connected, or
 * empty if none. Under LC's tree-plus-one-edge promise, this is the last edge on
 * the unique cycle. Parallel edges/self-loops supported; input unchanged.
 * Pattern: union-find. Sets are precisely connected components of accepted edges.
 * Same representatives mean an existing path, so adding this edge closes a cycle.
 * Different representatives merge components without creating a cycle.
 * Union by size and path halving give O(m alpha(m)) amortized time, O(m)
 * auxiliary, O(1) output/call stack. DSU is for undirected connectivity, NOT
 * directed cycle detection (compare CourseSchedule).
 */
public class RedundantConnection {
    public int[] solve(int[][] edges) {
        Objects.requireNonNull(edges);
        int n = edges.length;
        for (int[] edge : edges)
            if (edge == null || edge.length != 2 || edge[0] < 1 || edge[0] > n
                    || edge[1] < 1 || edge[1] > n) throw new IllegalArgumentException("invalid edge");
        int[] parent = new int[n + 1], size = new int[n + 1];
        for (int i = 1; i <= n; i++) { parent[i] = i; size[i] = 1; }
        for (int[] edge : edges) {
            int a = find(parent, edge[0]), b = find(parent, edge[1]);
            if (a == b) return edge.clone();
            if (size[a] < size[b]) { int swap = a; a = b; b = swap; }
            parent[b] = a;
            size[a] += size[b];
        }
        return new int[0];
    }

    private int find(int[] parent, int node) {
        while (node != parent[node]) {
            parent[node] = parent[parent[node]];
            node = parent[node];
        }
        return node;
    }
}
