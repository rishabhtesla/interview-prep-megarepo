import java.util.Objects;

/**
 * LC 122 (Medium): maximize profit with unlimited non-overlapping transactions.
 * https://leetcode.com/problems/best-time-to-buy-and-sell-stock-ii/
 *
 * <p>Contract: nonnegative prices; empty input returns 0. O(n) time, O(1) auxiliary.
 */
public class BestTimeToBuyAndSellStockII {
  public int solve(int[] prices) {
    Objects.requireNonNull(prices);
    long profit = 0;
    for (int i = 1; i < prices.length; i++) {
      if (prices[i] < 0 || prices[i - 1] < 0) {
        throw new IllegalArgumentException("prices must be nonnegative");
      }

      // Every positive day-to-day rise can be collected independently.
      if (prices[i] > prices[i - 1]) {
        profit += prices[i] - prices[i - 1];
      }
    }
    if (profit > Integer.MAX_VALUE) {
      throw new ArithmeticException("profit does not fit in int");
    }
    return (int) profit;
  }
}
