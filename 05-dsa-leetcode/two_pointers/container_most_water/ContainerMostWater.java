import java.util.Objects;

/**
 * LC 11 (Medium): maximize width times the shorter of two vertical heights.
 * https://leetcode.com/problems/container-with-most-water/
 *
 * Contract: non-null nonnegative heights, validated; fewer than two -> 0.
 * Returns long area; input unchanged.
 * Pattern: greedy two pointers. Holding the shorter wall while reducing width
 * cannot improve area: the limiting height stays at most that wall's height.
 * Therefore discard a shortest endpoint after measuring the current pair.
 * Repeating eliminates only pairs unable to beat an already considered pair.
 * O(n) time, O(1) auxiliary, output and stack.
 */
public class ContainerMostWater {
    public long solve(int[] heights) {
        Objects.requireNonNull(heights);
        for (int height : heights) if (height < 0) throw new IllegalArgumentException("negative height");
        int left = 0, right = heights.length - 1;
        long best = 0;
        while (left < right) {
            best = Math.max(best, (long) Math.min(heights[left], heights[right]) * (right - left));
            if (heights[left] <= heights[right]) left++;
            else right--;
        }
        return best;
    }
}
