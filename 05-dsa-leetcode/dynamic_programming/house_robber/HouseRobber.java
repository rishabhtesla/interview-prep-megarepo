import java.util.Objects;

/**
 * LC 198 (Medium): maximum sum of nonadjacent house values along a line.
 * https://leetcode.com/problems/house-robber/
 *
 * Contract: non-null nonnegative ints (validated); empty -> 0, long result.
 * No mutation. Houses are linear, not circular.
 * Pattern: choose/skip rolling DP. For prefix ending at i, either skip i and
 * keep best[i-1], or take i and combine value[i] with best[i-2]. These exhaust
 * valid optimal possibilities because taking i forbids exactly its predecessor.
 * twoBack/oneBack store those prefix optima before the update. Greedily selecting
 * the locally largest house can fail (e.g., [2,3,2]).
 * O(n) time, O(1) auxiliary/output/stack; long safely holds sums of int-array values.
 * Follow-up: circular houses require max(exclude first, exclude last).
 */
public class HouseRobber {
    public long solve(int[] houses) {
        Objects.requireNonNull(houses);
        long twoBack = 0, oneBack = 0;
        for (int value : houses) {
            if (value < 0) throw new IllegalArgumentException("negative house value");
            long current = Math.max(oneBack, twoBack + value);
            twoBack = oneBack;
            oneBack = current;
        }
        return oneBack;
    }
}
