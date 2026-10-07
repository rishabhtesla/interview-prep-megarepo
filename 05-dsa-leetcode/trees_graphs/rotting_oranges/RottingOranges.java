import java.util.*;

/**
 * LC 994 (Medium): minutes for rot to reach every fresh orange via four-neighbor
 * steps from all initially rotten oranges simultaneously.
 * https://leetcode.com/problems/rotting-oranges/
 *
 * Contract: non-null rectangular grid with 0=empty,1=fresh,2=rotten; malformed
 * rows/values throw IllegalArgumentException before mutation. Empty/no fresh -> 0;
 * unreachable fresh -> -1. Mutates every reachable fresh cell to 2, even on failure.
 * Pattern: multi-source BFS. All sources start at distance zero; processing a
 * whole queue layer simulates one minute, and marking on enqueue prevents duplicate
 * infections. First arrival is a shortest path because all earlier layers finish
 * first. Track remaining fresh cells to avoid an unnecessary final minute.
 * O(rc) time, O(rc) auxiliary queue, O(1) output/call stack.
 */
public class RottingOranges {
    public int solve(int[][] grid) {
        Objects.requireNonNull(grid);
        int cols = grid.length == 0 ? 0 : grid[0] == null ? -1 : grid[0].length;
        for (int[] row : grid) {
            if (row == null || row.length != cols) throw new IllegalArgumentException("rectangular grid required");
            for (int value : row) if (value < 0 || value > 2) throw new IllegalArgumentException("invalid cell");
        }
        Deque<int[]> queue = new ArrayDeque<>();
        int fresh = 0, minutes = 0;
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == 1) fresh++;
                else if (grid[r][c] == 2) queue.add(new int[]{r, c});
            }
        }
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (fresh > 0 && !queue.isEmpty()) {
            int layer = queue.size();
            for (int i = 0; i < layer; i++) {
                int[] cell = queue.remove();
                for (int[] d : directions) {
                    int r = cell[0] + d[0], c = cell[1] + d[1];
                    if (r >= 0 && r < grid.length && c >= 0 && c < cols && grid[r][c] == 1) {
                        grid[r][c] = 2;
                        fresh--;
                        queue.add(new int[]{r, c});
                    }
                }
            }
            minutes++;
        }
        return fresh == 0 ? minutes : -1;
    }
}
