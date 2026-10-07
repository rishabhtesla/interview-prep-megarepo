import java.util.*;

/**
 * LC 1 (Easy): return two distinct zero-based indices whose values total target.
 * https://leetcode.com/problems/two-sum/
 *
 * Contract: non-null int array; empty/singleton/no match returns an empty array.
 * Any matching pair is acceptable. Input is unchanged; all int values are supported.
 * Pattern: complement lookup. Before iteration i, the map contains only indices
 * less than i. Looking up before inserting prevents using the same element twice.
 * Every pair has a later endpoint, at which point its earlier endpoint is present.
 * Use long subtraction so an overflowing int complement cannot create a false pair.
 * Expected time O(n), auxiliary O(n), output O(1), call stack O(1).
 * Follow-up: sorting permits two pointers but loses original indices unless retained.
 */
public class TwoSum {
    public int[] solve(int[] nums, int target) {
        Objects.requireNonNull(nums);
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            long complement = (long) target - nums[i];
            if (complement >= Integer.MIN_VALUE && complement <= Integer.MAX_VALUE
                    && seen.containsKey((int) complement)) {
                return new int[]{seen.get((int) complement), i};
            }
            seen.put(nums[i], i);
        }
        return new int[0];
    }
}
