import java.util.Objects;

/**
 * LC 33 (Medium): locate a value after a strictly increasing array is rotated.
 * https://leetcode.com/problems/search-in-rotated-sorted-array/
 *
 * Contract: non-null rotation of distinct sorted ints (trusted precondition).
 * Empty or absent -> -1; no mutation. Duplicates are deliberately unsupported.
 * Pattern: identify the sorted half. A single rotation leaves at least one half
 * sorted. Test whether target falls inside that half's inclusive/exclusive range;
 * keep it exactly then, otherwise keep the other half. Target, if present, remains
 * in [low,high]; equality is checked before shrinking past mid.
 * O(log(n+1)) time, O(1) auxiliary/output/stack. With duplicates, equal endpoints
 * can hide the pivot and force linear-time ambiguity resolution.
 */
public class SearchRotated {
    public int solve(int[] nums, int target) {
        Objects.requireNonNull(nums);
        int low = 0, high = nums.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (nums[mid] == target) return mid;
            if (nums[low] <= nums[mid]) {
                if (nums[low] <= target && target < nums[mid]) high = mid - 1;
                else low = mid + 1;
            } else {
                if (nums[mid] < target && target <= nums[high]) low = mid + 1;
                else high = mid - 1;
            }
        }
        return -1;
    }
}
