import java.util.Objects;

/**
 * LC 80 (Medium): keep at most two copies in a sorted array.
 * https://leetcode.com/problems/remove-duplicates-from-sorted-array-ii/
 *
 * <p>Contract: returns the compact-prefix length and mutates the input in place. O(n) time, O(1)
 * auxiliary.
 */
public class RemoveDuplicatesII {
  public int solve(int[] nums) {
    Objects.requireNonNull(nums);
    int writeIndex = 0;
    for (int value : nums) {
      // Looking two positions back rejects a third copy of the same sorted value.
      if (writeIndex < 2 || value != nums[writeIndex - 2]) {
        nums[writeIndex] = value;
        writeIndex++;
      }
    }
    return writeIndex;
  }

  public static void main(String[] args) {
    int[] nums = {0, 0, 1, 1, 1, 1, 2, 3, 3};
    int length = new RemoveDuplicatesII().solve(nums);
    int[] expectedPrefix = {0, 0, 1, 1, 2, 3, 3};
    if (length != expectedPrefix.length
        || !java.util.Arrays.equals(expectedPrefix, java.util.Arrays.copyOf(nums, length))) {
      throw new AssertionError("unexpected compact prefix: " + java.util.Arrays.toString(nums));
    }
    System.out.println(
        "RemoveDuplicatesII: " + java.util.Arrays.toString(nums) + ", length=" + length);
  }
}
