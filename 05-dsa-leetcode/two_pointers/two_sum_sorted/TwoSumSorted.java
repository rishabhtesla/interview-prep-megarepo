import java.util.Objects;

/**
 * LC 167 (Medium): find two values with a target sum in a sorted array.
 * https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/
 *
 * Contract: non-null, nondecreasing array (ordering is a precondition, not scanned);
 * return one-based distinct indices or empty when absent. Input unchanged.
 * Pattern: ordered elimination. If endpoint sum is too small, pairing the left
 * endpoint with anything else is no larger, so discard it. Symmetrically discard
 * the right endpoint when the sum is too large. Thus no discarded endpoint could
 * belong to a remaining solution. long addition avoids wraparound.
 * O(n) time, O(1) auxiliary/output/stack.
 */
public class TwoSumSorted {
    public int[] solve(int[] nums, int target) {
        Objects.requireNonNull(nums);
        int left = 0, right = nums.length - 1;
        while (left < right) {
            long sum = (long) nums[left] + nums[right];
            if (sum == target) return new int[]{left + 1, right + 1};
            if (sum < target) left++;
            else right--;
        }
        return new int[0];
    }
}
