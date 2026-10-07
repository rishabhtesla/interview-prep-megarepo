import java.util.*;

/**
 * LC 207 (Medium): can every course be completed given prerequisite pairs?
 * https://leetcode.com/problems/course-schedule/
 *
 * Contract: n >= 0; non-null pairs [course,prerequisite], labels in [0,n).
 * Malformed pairs/labels throw IllegalArgumentException; duplicate edges supported.
 * No courses/no edges -> true. Inputs unchanged.
 * Pattern: Kahn topological elimination. indegree is the number of prerequisite
 * edges from unprocessed nodes. A zero-indegree node can be taken now; removing
 * its outgoing edges preserves the invariant. If fewer than n nodes are removed,
 * the residual finite directed graph has no source and therefore contains a cycle.
 * Conversely cycles never reach zero indegree, so n removed iff scheduling is possible.
 * O(V+E) time and auxiliary adjacency/indegrees/queue, O(1) result/call stack.
 */
public class CourseSchedule {
    public boolean solve(int n, int[][] prerequisites) {
        Objects.requireNonNull(prerequisites);
        if (n < 0) throw new IllegalArgumentException("negative course count");
        List<List<Integer>> next = new ArrayList<>();
        for (int i = 0; i < n; i++) next.add(new ArrayList<>());
        int[] indegree = new int[n];
        for (int[] edge : prerequisites) {
            if (edge == null || edge.length != 2 || edge[0] < 0 || edge[0] >= n
                    || edge[1] < 0 || edge[1] >= n) throw new IllegalArgumentException("invalid prerequisite");
            next.get(edge[1]).add(edge[0]);
            indegree[edge[0]]++;
        }
        Deque<Integer> ready = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indegree[i] == 0) ready.add(i);
        int completed = 0;
        while (!ready.isEmpty()) {
            int course = ready.remove();
            completed++;
            for (int dependent : next.get(course))
                if (--indegree[dependent] == 0) ready.add(dependent);
        }
        return completed == n;
    }
}
