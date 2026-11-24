import java.util.Objects;

/**
 * LC 26 (Easy): keep one copy of each value in a sorted array.
 * https://leetcode.com/problems/remove-duplicates-from-sorted-array/
 *
 * <p>Contract: returns the compact-prefix length and mutates the input in place. O(n) time, O(1)
 * auxiliary.
 */
public class RemoveDuplicates {
  public int solve(int[] nums) {
    Objects.requireNonNull(nums);
    if (nums.length == 0) {
      return 0;
    }
    int writeIndex = 1;
    for (int readIndex = 1; readIndex < nums.length; readIndex++) {
      // The prefix [0, writeIndex) is sorted and contains one copy each.
      if (nums[readIndex] != nums[writeIndex - 1]) {
        nums[writeIndex] = nums[readIndex];
        writeIndex++;
      }
    }
    return writeIndex;
  }
}
