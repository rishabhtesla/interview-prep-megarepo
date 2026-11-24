import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * LC 128 (Medium): find the length of the longest run of consecutive integer values.
 * https://leetcode.com/problems/longest-consecutive-sequence/
 *
 * <p>Contract: non-null array of any ints; duplicates ignored, empty -> 0; no mutation. Pattern:
 * hash-set sequence starts. Expand only a value without a predecessor. Each maximal run has one
 * such start, so although the code is nested, each unique value participates in at most one
 * expansion. Starting at every value is quadratic. Guard integer endpoints: MIN_VALUE has no
 * predecessor and MAX_VALUE no successor. Expected O(n) time, O(n) auxiliary, O(1) output and call
 * stack.
 */
public class LongestConsecutive {
  public int solve(int[] nums) {
    Objects.requireNonNull(nums);
    Set<Integer> values = new HashSet<>();
    for (int value : nums) {
      values.add(value);
    }
    int best = 0;
    for (int value : values) {
      if (value != Integer.MIN_VALUE && values.contains(value - 1)) {
        continue;
      }
      int end = value;
      int length = 1;
      // Only a sequence start enters this loop, so each distinct value is
      // expanded at most once across the whole algorithm.
      while (end != Integer.MAX_VALUE && values.contains(end + 1)) {
        end++;
        length++;
      }
      best = Math.max(best, length);
    }
    return best;
  }

  public static void main(String[] args) {
    int[] nums = {100, 4, 200, 1, 3, 2};
    int answer = new LongestConsecutive().solve(nums);
    if (answer != 4) {
      throw new AssertionError("expected 4, got " + answer);
    }
    System.out.println("LongestConsecutive: " + java.util.Arrays.toString(nums) + " -> " + answer);
  }
}
