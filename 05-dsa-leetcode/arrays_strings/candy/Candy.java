import java.util.Objects;

/**
 * LC 135 (Hard): minimum candies satisfying both neighbor inequalities.
 * https://leetcode.com/problems/candy/
 *
 * Contract: nonnegative ratings; returns the minimum total. Two directional
 * passes capture left and right constraints. O(n) time, O(n) auxiliary.
 */
public class Candy {
    public int solve(int[] ratings) {
        Objects.requireNonNull(ratings);
        int[] candies = new int[ratings.length];
        java.util.Arrays.fill(candies, 1);
        for (int i = 1; i < ratings.length; i++) { if (ratings[i] > ratings[i - 1]) candies[i] = candies[i - 1] + 1; }
        long total = 0;
        for (int i = ratings.length - 2; i >= 0; i--) { if (ratings[i] > ratings[i + 1]) candies[i] = Math.max(candies[i], candies[i + 1] + 1); total += candies[i + 1]; }
        if (ratings.length > 0) total += candies[0];
        if (total > Integer.MAX_VALUE) throw new ArithmeticException();
        return (int) total;
    }
}
