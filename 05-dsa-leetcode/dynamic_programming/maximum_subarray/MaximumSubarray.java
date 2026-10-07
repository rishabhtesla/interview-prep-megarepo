import java.util.Objects;

/**
 * LC 53 (Medium): maximum sum of a NONEMPTY contiguous segment.
 * https://leetcode.com/problems/maximum-subarray/
 *
 * Contract: non-null NONEMPTY int array; empty throws IllegalArgumentException.
 * Returns long to avoid int sum overflow; no mutation.
 * Pattern: Kadane rolling DP. bestEndingHere either starts at current value or
 * extends the best segment ending immediately before it. These are the only
 * possibilities for a contiguous segment ending here. Global best maximizes
 * those endpoints. Initializing with nums[0], not zero, respects nonemptiness
 * and correctly handles all-negative input. Negative prefix sums are discarded.
 * O(n) time, O(1) auxiliary/output/stack. Compare with HouseRobber: contiguity and
 * nonadjacency are different constraints and lead to different recurrences.
 */
public class MaximumSubarray {
    public long solve(int[] nums) {
        Objects.requireNonNull(nums);
        if (nums.length == 0) throw new IllegalArgumentException("nonempty array required");
        long ending = nums[0], best = nums[0];
        for (int i = 1; i < nums.length; i++) {
            ending = Math.max(nums[i], ending + nums[i]);
            best = Math.max(best, ending);
        }
        return best;
    }
}
