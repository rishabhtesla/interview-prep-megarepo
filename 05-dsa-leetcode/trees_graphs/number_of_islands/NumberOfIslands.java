import java.util.*;

/**
 * LC 200 (Medium): count four-directionally connected land components.
 * https://leetcode.com/problems/number-of-islands/
 *
 * Contract: non-null rectangular grid of '0'/'1'; null/ragged rows or other
 * symbols throw IllegalArgumentException before mutation. Empty dimensions -> 0.
 * DESTRUCTIVE: all land is changed to '0'; copy the grid if it must be reused.
 * Pattern: flood fill. A new unvisited land cell starts exactly one component.
 * BFS reaches every cell in that component and nothing outside it. Mark ON ENQUEUE,
 * not dequeue, so multiple neighbors cannot schedule the same cell repeatedly.
 * Each cell is examined O(1) times: O(rc) time, O(rc) worst-case auxiliary queue,
 * O(1) output/call stack. Iterative traversal avoids recursive flood-fill overflow.
 */
public class NumberOfIslands {
    public int solve(char[][] grid) {
        Objects.requireNonNull(grid);
        int cols = grid.length == 0 ? 0 : grid[0] == null ? -1 : grid[0].length;
        for (char[] row : grid) {
            if (row == null || row.length != cols) throw new IllegalArgumentException("rectangular grid required");
            for (char c : row) if (c != '0' && c != '1') throw new IllegalArgumentException("binary grid required");
        }
        int islands = 0;
        Deque<int[]> queue = new ArrayDeque<>();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < cols; col++) {
                if (grid[row][col] != '1') continue;
                islands++;
                grid[row][col] = '0';
                queue.add(new int[]{row, col});
                while (!queue.isEmpty()) {
                    int[] cell = queue.remove();
                    for (int[] direction : directions) {
                        int r = cell[0] + direction[0], c = cell[1] + direction[1];
                        if (r >= 0 && r < grid.length && c >= 0 && c < cols && grid[r][c] == '1') {
                            grid[r][c] = '0';
                            queue.add(new int[]{r, c});
                        }
                    }
                }
            }
        }
        return islands;
    }
}
