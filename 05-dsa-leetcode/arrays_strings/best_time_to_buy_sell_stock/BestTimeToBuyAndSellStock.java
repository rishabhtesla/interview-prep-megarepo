import java.util.Objects;

/**
 * LC 121 (Easy): maximize one buy/sell transaction.
 * https://leetcode.com/problems/best-time-to-buy-and-sell-stock/
 *
 * Contract: nonnegative prices; empty input returns 0. O(n) time, O(1) auxiliary.
 */
public class BestTimeToBuyAndSellStock {
    public int solve(int[] prices) {
        Objects.requireNonNull(prices);
        int low = Integer.MAX_VALUE, best = 0;
        for (int price : prices) { if (price < 0) throw new IllegalArgumentException(); low = Math.min(low, price); best = Math.max(best, price - low); }
        return best;
    }
}
