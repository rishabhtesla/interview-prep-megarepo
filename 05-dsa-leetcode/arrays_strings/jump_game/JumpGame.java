import java.util.Objects;

/**
 * LC 55 (Medium): determine whether the last index is reachable.
 * https://leetcode.com/problems/jump-game/
 *
 * <p>Contract: nonnegative jumps; empty input is reachable. Greedy farthest reach proves every
 * index at or before it can be visited. O(n) time, O(1) auxiliary.
 */
public class JumpGame {
  public boolean solve(int[] nums) {
    Objects.requireNonNull(nums);
    int farthestReachable = 0;
    for (int i = 0; i < nums.length; i++) {
      if (nums[i] < 0) {
        throw new IllegalArgumentException("jumps must be nonnegative");
      }
      if (i > farthestReachable) {
        // The current index is beyond every reachable prefix.
        return false;
      }
      farthestReachable = Math.max(farthestReachable, i + nums[i]);
    }
    return true;
  }

  public static void main(String[] args) {
    int[] nums = {2, 3, 1, 1, 4};
    boolean answer = new JumpGame().solve(nums);
    if (!answer) {
      throw new AssertionError("expected the last index to be reachable");
    }
    System.out.println("JumpGame: " + java.util.Arrays.toString(nums) + " -> " + answer);
  }
}
