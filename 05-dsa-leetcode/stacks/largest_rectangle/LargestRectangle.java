import java.util.*;

/**
 * LC 84 (Hard): largest rectangle formed by adjacent unit-width histogram bars.
 * https://leetcode.com/problems/largest-rectangle-in-histogram/
 *
 * Contract: non-null nonnegative heights (validated); empty -> 0; long result,
 * no input mutation.
 * Pattern: increasing index stack. On a strictly lower bar, popped height cannot
 * extend rightward. Its new stack predecessor bounds its leftward extent; width
 * is right - predecessor - 1. Equal-height bars remain, with the leftmost eventually
 * obtaining their full span. A virtual zero at n flushes remaining positive bars.
 * Every maximal rectangle has a limiting bar whose full legal width is considered.
 * O(n) amortized time (each index pushed/popped once), O(n) auxiliary, O(1)
 * output/call stack. A long product avoids height*width int overflow.
 */
public class LargestRectangle {
    public long solve(int[] heights) {
        Objects.requireNonNull(heights);
        for (int h : heights) if (h < 0) throw new IllegalArgumentException("negative height");
        Deque<Integer> stack = new ArrayDeque<>();
        long best = 0;
        for (int right = 0; right <= heights.length; right++) {
            int current = right == heights.length ? 0 : heights[right];
            while (!stack.isEmpty() && heights[stack.peek()] > current) {
                int height = heights[stack.pop()];
                int left = stack.isEmpty() ? -1 : stack.peek();
                best = Math.max(best, (long) height * (right - left - 1));
            }
            if (right < heights.length) stack.push(right);
        }
        return best;
    }
}
