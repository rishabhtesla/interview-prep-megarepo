import java.util.Objects;

/**
 * LC 189 (Medium): rotate an array right by k steps in place.
 * https://leetcode.com/problems/rotate-array/
 *
 * <p>Contract: negative k is invalid; input is mutated and returned. Pattern: reverse whole array,
 * then each block. O(n) time, O(1) auxiliary.
 */
public class RotateArray {
  public int[] solve(int[] nums, int k) {
    Objects.requireNonNull(nums);
    if (k < 0) {
      throw new IllegalArgumentException("rotation must be nonnegative");
    }
    if (nums.length == 0) {
      return nums;
    }
    int rotations = k % nums.length;

    // Reversing the whole array and then both blocks moves the suffix to front.
    reverse(nums, 0, nums.length - 1);
    reverse(nums, 0, rotations - 1);
    reverse(nums, rotations, nums.length - 1);
    return nums;
  }

  private void reverse(int[] values, int low, int high) {
    while (low < high) {
      int temporary = values[low];
      values[low] = values[high];
      values[high] = temporary;
      low++;
      high--;
    }
  }
}
