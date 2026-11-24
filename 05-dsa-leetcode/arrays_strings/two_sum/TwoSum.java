import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * LC 1 (Easy): return two distinct zero-based indices whose values total target.
 * https://leetcode.com/problems/two-sum/
 *
 * <p>Contract: non-null int array; empty/singleton/no match returns an empty array. Any matching
 * pair is acceptable. Input is unchanged; all int values are supported. Pattern: complement lookup.
 * Before iteration i, the map contains only indices less than i. Looking up before inserting
 * prevents using the same element twice. Every pair has a later endpoint, at which point its
 * earlier endpoint is present. Use long subtraction so an overflowing int complement cannot create
 * a false pair. Expected time O(n), auxiliary O(n), output O(1), call stack O(1). Follow-up:
 * sorting permits two pointers but loses original indices unless retained.
 */
public class TwoSum {
  public int[] solve(int[] nums, int target) {
    Objects.requireNonNull(nums);
    Map<Integer, Integer> seen = new HashMap<>();
    for (int index = 0; index < nums.length; index++) {
      long complement = (long) target - nums[index];
      boolean complementFitsInt =
          complement >= Integer.MIN_VALUE && complement <= Integer.MAX_VALUE;
      if (complementFitsInt && seen.containsKey((int) complement)) {
        return new int[] {seen.get((int) complement), index};
      }

      // The map contains only earlier indices, so the same element is never reused.
      seen.put(nums[index], index);
    }
    return new int[0];
  }

  public static void main(String[] args) {
    int[] nums = {2, 7, 11, 15};
    int[] answer = new TwoSum().solve(nums, 9);
    int[] expected = {0, 1};
    if (!java.util.Arrays.equals(expected, answer)) {
      throw new AssertionError(
          "expected "
              + java.util.Arrays.toString(expected)
              + ", got "
              + java.util.Arrays.toString(answer));
    }
    System.out.println(
        "TwoSum: "
            + java.util.Arrays.toString(nums)
            + ", target=9 -> "
            + java.util.Arrays.toString(answer));
  }
}
