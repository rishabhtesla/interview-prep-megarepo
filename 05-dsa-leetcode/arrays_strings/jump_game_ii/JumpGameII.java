import java.util.Objects;

/**
 * LC 45 (Medium): minimum jumps to reach the final index.
 * https://leetcode.com/problems/jump-game-ii/
 *
 * <p>Contract: nonnegative jumps; unreachable returns -1. Greedy layer expansion counts the fewest
 * intervals. O(n) time, O(1) auxiliary.
 */
public class JumpGameII {
  public int solve(int[] nums) {
    Objects.requireNonNull(nums);
    for (int jump : nums) {
      if (jump < 0) {
        throw new IllegalArgumentException("jumps must be nonnegative");
      }
    }
    if (nums.length < 2) {
      return 0;
    }
    int jumps = 0;
    int currentLayerEnd = 0;
    int farthestReachable = 0;
    for (int i = 0; i < nums.length - 1; i++) {
      if (i > farthestReachable) {
        return -1;
      }
      farthestReachable = Math.max(farthestReachable, i + nums[i]);
      if (i == currentLayerEnd) {
        // All indexes in this layer use one jump; choose its farthest next layer.
        jumps++;
        currentLayerEnd = farthestReachable;
      }
    }
    return currentLayerEnd >= nums.length - 1 ? jumps : -1;
  }
}
