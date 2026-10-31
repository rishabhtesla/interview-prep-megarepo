import java.util.Objects;

/**
 * LC 55 (Medium): determine whether the last index is reachable.
 * https://leetcode.com/problems/jump-game/
 *
 * Contract: nonnegative jumps; empty input is reachable. Greedy farthest reach
 * proves every index at or before it can be visited. O(n) time, O(1) auxiliary.
 */
public class JumpGame {
    public boolean solve(int[] nums) {
        Objects.requireNonNull(nums);
        int reach = 0;
        for (int i = 0; i < nums.length; i++) { if (nums[i] < 0) throw new IllegalArgumentException(); if (i > reach) return false; reach = Math.max(reach, i + nums[i]); }
        return true;
    }
}
