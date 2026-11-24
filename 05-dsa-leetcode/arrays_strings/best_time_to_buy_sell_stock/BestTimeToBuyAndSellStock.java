import java.util.Objects;

/**
 * LC 121 (Easy): maximize one buy/sell transaction.
 * https://leetcode.com/problems/best-time-to-buy-and-sell-stock/
 *
 * <p>Contract: nonnegative prices; empty input returns 0. O(n) time, O(1) auxiliary.
 */
public class BestTimeToBuyAndSellStock {
  public int solve(int[] prices) {
    Objects.requireNonNull(prices);
    int lowestPriceSeen = Integer.MAX_VALUE;
    int bestProfit = 0;
    for (int price : prices) {
      if (price < 0) {
        throw new IllegalArgumentException("prices must be nonnegative");
      }

      // Invariant: lowestPriceSeen is the cheapest buy available before today.
      lowestPriceSeen = Math.min(lowestPriceSeen, price);
      int profitIfSoldToday = price - lowestPriceSeen;
      bestProfit = Math.max(bestProfit, profitIfSoldToday);
    }
    return bestProfit;
  }

  public static void main(String[] args) {
    int[] prices = {7, 1, 5, 3, 6, 4};
    int answer = new BestTimeToBuyAndSellStock().solve(prices);
    if (answer != 5) {
      throw new AssertionError("expected 5, got " + answer);
    }
    System.out.println(
        "BestTimeToBuyAndSellStock: " + java.util.Arrays.toString(prices) + " -> " + answer);
  }
}
