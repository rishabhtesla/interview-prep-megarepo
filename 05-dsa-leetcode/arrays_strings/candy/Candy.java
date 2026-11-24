import java.util.Arrays;
import java.util.Objects;

/**
 * LC 135 (Hard): minimum candies satisfying both neighbor inequalities.
 * https://leetcode.com/problems/candy/
 *
 * <p>Contract: nonnegative ratings; returns the minimum total. Two directional passes capture left
 * and right constraints. O(n) time, O(n) auxiliary.
 */
public class Candy {
  public int solve(int[] ratings) {
    Objects.requireNonNull(ratings);
    int[] candies = new int[ratings.length];
    Arrays.fill(candies, 1);

    // Left-to-right pass: every child higher than its left neighbor gets one
    // more candy than the already-correct left-side requirement.
    for (int i = 1; i < ratings.length; i++) {
      if (ratings[i] > ratings[i - 1]) {
        candies[i] = candies[i - 1] + 1;
      }
    }

    // Right-to-left pass adds the right-side requirement. Keep the larger
    // value because a child may need to satisfy both neighbors.
    long total = 0;
    for (int i = ratings.length - 2; i >= 0; i--) {
      if (ratings[i] > ratings[i + 1]) {
        candies[i] = Math.max(candies[i], candies[i + 1] + 1);
      }
      total += candies[i + 1];
    }
    if (ratings.length > 0) {
      total += candies[0];
    }
    if (total > Integer.MAX_VALUE) {
      throw new ArithmeticException("candy total does not fit in int");
    }
    return (int) total;
  }
}
