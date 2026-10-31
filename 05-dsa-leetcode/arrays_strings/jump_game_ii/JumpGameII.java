import java.util.Objects;

/**
 * LC 45 (Medium): minimum jumps to reach the final index.
 * https://leetcode.com/problems/jump-game-ii/
 *
 * Contract: nonnegative jumps; unreachable returns -1. Greedy layer expansion
 * counts the fewest intervals. O(n) time, O(1) auxiliary.
 */
public class JumpGameII {
    public int solve(int[] nums) {
        Objects.requireNonNull(nums);
        if (nums.length < 2) return 0;
        int jumps = 0, end = 0, farthest = 0;
        for (int i = 0; i < nums.length - 1; i++) { if (nums[i] < 0) throw new IllegalArgumentException(); if (i > farthest) return -1; farthest = Math.max(farthest, i + nums[i]); if (i == end) { jumps++; end = farthest; } }
        return end >= nums.length - 1 ? jumps : -1;
    }
}
