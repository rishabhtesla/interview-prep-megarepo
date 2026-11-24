import java.util.Objects;

/**
 * LC 27 (Easy): remove all occurrences of val in place.
 * https://leetcode.com/problems/remove-element/
 *
 * <p>Contract: returns the retained prefix length and leaves that prefix compact; order is
 * preserved and the suffix is unspecified. O(n) time, O(1) auxiliary.
 */
public class RemoveElement {
  public int solve(int[] nums, int val) {
    Objects.requireNonNull(nums);
    int writeIndex = 0;
    for (int value : nums) {
      // The prefix before writeIndex contains exactly the retained values.
      if (value != val) {
        nums[writeIndex] = value;
        writeIndex++;
      }
    }
    return writeIndex;
  }
}
