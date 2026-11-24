import java.util.Objects;

/**
 * LC 153 (Medium): minimum value in a rotated strictly increasing array.
 * https://leetcode.com/problems/find-minimum-in-rotated-sorted-array/
 *
 * <p>Contract: nonempty, non-null rotation of distinct sorted ints. Empty throws
 * IllegalArgumentException; ordering/distinctness are trusted; no mutation. Pattern: compare
 * midpoint to right endpoint. If mid is larger, the wraparound minimum is strictly to its right.
 * Otherwise mid lies in the final sorted segment, so the minimum is at mid or left of it. Keeping
 * mid via high=mid is crucial. Invariant: [low,high] contains the minimum; each step reduces its
 * length until one candidate remains. O(log n) time, O(1) auxiliary/output/stack.
 */
public class FindRotatedMinimum {
  public int solve(int[] nums) {
    Objects.requireNonNull(nums);
    if (nums.length == 0) {
      throw new IllegalArgumentException("empty array");
    }
    int low = 0;
    int high = nums.length - 1;
    while (low < high) {
      int mid = low + (high - low) / 2;
      // The minimum always remains inside the inclusive interval [low, high].
      if (nums[mid] > nums[high]) {
        low = mid + 1;
      } else {
        high = mid;
      }
    }
    return nums[low];
  }
}
