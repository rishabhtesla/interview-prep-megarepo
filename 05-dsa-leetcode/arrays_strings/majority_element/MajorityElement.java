import java.util.Objects;

/**
 * LC 169 (Easy): find the element occurring more than n/2 times.
 * https://leetcode.com/problems/majority-element/
 *
 * <p>Contract: non-empty input is guaranteed to have a majority; input unchanged. Pattern:
 * Boyer-Moore cancellation. O(n) time, O(1) auxiliary.
 */
public class MajorityElement {
  public int solve(int[] nums) {
    Objects.requireNonNull(nums);
    if (nums.length == 0) {
      throw new IllegalArgumentException("a majority requires a nonempty array");
    }
    int candidate = 0;
    int count = 0;
    for (int num : nums) {
      if (count == 0) {
        candidate = num;
      }
      // Equal values support the candidate; different values cancel one vote.
      count += num == candidate ? 1 : -1;
    }
    return candidate;
  }
}
