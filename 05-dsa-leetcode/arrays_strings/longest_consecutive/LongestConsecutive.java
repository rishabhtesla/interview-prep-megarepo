import java.util.*;

/**
 * LC 128 (Medium): find the length of the longest run of consecutive integer values.
 * https://leetcode.com/problems/longest-consecutive-sequence/
 *
 * Contract: non-null array of any ints; duplicates ignored, empty -> 0; no mutation.
 * Pattern: hash-set sequence starts. Expand only a value without a predecessor.
 * Each maximal run has one such start, so although the code is nested, each unique
 * value participates in at most one expansion. Starting at every value is quadratic.
 * Guard integer endpoints: MIN_VALUE has no predecessor and MAX_VALUE no successor.
 * Expected O(n) time, O(n) auxiliary, O(1) output and call stack.
 */
public class LongestConsecutive {
    public int solve(int[] nums) {
        Objects.requireNonNull(nums);
        Set<Integer> values = new HashSet<>();
        for (int value : nums) values.add(value);
        int best = 0;
        for (int value : values) {
            if (value != Integer.MIN_VALUE && values.contains(value - 1)) continue;
            int end = value, length = 1;
            while (end != Integer.MAX_VALUE && values.contains(end + 1)) {
                end++;
                length++;
            }
            best = Math.max(best, length);
        }
        return best;
    }
}
