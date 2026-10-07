import java.util.Objects;

/**
 * LC 704 (Easy): find a target in a sorted array.
 * https://leetcode.com/problems/binary-search/
 *
 * Contract: non-null nondecreasing array; sortedness is a trusted precondition.
 * Returns the FIRST index if duplicates exist, otherwise -1. No mutation.
 * Pattern: lower bound in [low,high). All positions below low are < target;
 * all positions at/above high are >= target. Comparing mid preserves this
 * partition and strictly shrinks the interval. At convergence low is the first
 * not-less-than position; verify equality because an insertion point need not match.
 * O(log(n+1)) time, O(1) auxiliary/output/stack. Half-open intervals naturally
 * handle empty input; low + (high-low)/2 avoids midpoint overflow.
 */
public class BinarySearch {
    public int solve(int[] nums, int target) {
        Objects.requireNonNull(nums);
        int low = 0, high = nums.length;
        while (low < high) {
            int mid = low + (high - low) / 2;
            if (nums[mid] < target) low = mid + 1;
            else high = mid;
        }
        return low < nums.length && nums[low] == target ? low : -1;
    }
}
