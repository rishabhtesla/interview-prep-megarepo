import java.util.Objects;

/**
 * LC 169 (Easy): find the element occurring more than n/2 times.
 * https://leetcode.com/problems/majority-element/
 *
 * Contract: non-empty input is guaranteed to have a majority; input unchanged.
 * Pattern: Boyer-Moore cancellation. O(n) time, O(1) auxiliary.
 */
public class MajorityElement {
    public int solve(int[] nums) {
        Objects.requireNonNull(nums);
        if (nums.length == 0) throw new IllegalArgumentException();
        int candidate = 0, count = 0;
        for (int num : nums) {
            if (count == 0) candidate = num;
            count += num == candidate ? 1 : -1;
        }
        return candidate;
    }
}
