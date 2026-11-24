import java.util.Objects;

/**
 * LC 42 (Hard): calculate water trapped between elevation bars.
 * https://leetcode.com/problems/trapping-rain-water/
 *
 * <p>Contract: nonnegative heights; returns the trapped volume. Pattern: two pointers maintain the
 * lower side's maximum. O(n) time, O(1) auxiliary.
 */
public class TrappingRainWater {
  public int solve(int[] height) {
    Objects.requireNonNull(height);
    int left = 0;
    int right = height.length - 1;
    int leftMax = 0;
    int rightMax = 0;
    long water = 0;
    while (left <= right) {
      if (height[left] < 0 || height[right] < 0) {
        throw new IllegalArgumentException("heights must be nonnegative");
      }
      if (height[left] <= height[right]) {
        // The right wall is at least as high, so leftMax determines this bar's water.
        leftMax = Math.max(leftMax, height[left]);
        water += leftMax - height[left];
        left++;
      } else {
        // Symmetric argument: rightMax determines this bar's water.
        rightMax = Math.max(rightMax, height[right]);
        water += rightMax - height[right];
        right--;
      }
    }
    if (water > Integer.MAX_VALUE) {
      throw new ArithmeticException("water volume does not fit in int");
    }
    return (int) water;
  }

  public static void main(String[] args) {
    int[] heights = {0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1};
    int answer = new TrappingRainWater().solve(heights);
    if (answer != 6) {
      throw new AssertionError("expected 6, got " + answer);
    }
    System.out.println(
        "TrappingRainWater: " + java.util.Arrays.toString(heights) + " -> " + answer);
  }
}
