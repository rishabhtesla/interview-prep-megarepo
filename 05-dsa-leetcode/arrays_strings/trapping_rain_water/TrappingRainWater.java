import java.util.Objects;

/**
 * LC 42 (Hard): calculate water trapped between elevation bars.
 * https://leetcode.com/problems/trapping-rain-water/
 *
 * Contract: nonnegative heights; returns the trapped volume. Pattern:
 * two pointers maintain the lower side's maximum. O(n) time, O(1) auxiliary.
 */
public class TrappingRainWater {
    public int solve(int[] height) {
        Objects.requireNonNull(height);
        int left = 0, right = height.length - 1, leftMax = 0, rightMax = 0; long water = 0;
        while (left <= right) {
            if (height[left] < 0 || height[right] < 0) throw new IllegalArgumentException();
            if (height[left] <= height[right]) { leftMax = Math.max(leftMax, height[left]); water += leftMax - height[left++]; }
            else { rightMax = Math.max(rightMax, height[right]); water += rightMax - height[right--]; }
        }
        if (water > Integer.MAX_VALUE) throw new ArithmeticException();
        return (int) water;
    }
}
