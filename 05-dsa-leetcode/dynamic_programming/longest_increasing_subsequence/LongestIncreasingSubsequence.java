import java.util.Objects;

/**
 * LC 300 (Medium): length of a strictly increasing subsequence, not necessarily contiguous.
 * https://leetcode.com/problems/longest-increasing-subsequence/
 *
 * Contract: non-null arbitrary ints; empty -> 0; equal values cannot extend length.
 * No mutation. Returns length, not an actual subsequence.
 * Pattern: DP with binary-searched tails. tails[len-1] is the smallest achievable
 * ending value for an increasing subsequence of length len in the processed prefix.
 * A smaller tail dominates a larger one for every future extension. Replace the
 * first tail >= value; all previous tails are smaller, so a valid sequence can
 * extend to this length. Append only when all tails are smaller.
 * tails is sorted but its entries need NOT together form an actual subsequence.
 * O(n log(n+1)) time, O(n) auxiliary, O(1) output/stack.
 */
public class LongestIncreasingSubsequence {
    public int solve(int[] nums) {
        Objects.requireNonNull(nums);
        int[] tails = new int[nums.length];
        int length = 0;
        for (int value : nums) {
            int low = 0, high = length;
            while (low < high) {
                int mid = low + (high - low) / 2;
                if (tails[mid] < value) low = mid + 1;
                else high = mid;
            }
            tails[low] = value;
            if (low == length) length++;
        }
        return length;
    }
}
