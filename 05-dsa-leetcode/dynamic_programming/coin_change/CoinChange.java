import java.util.*;

/**
 * LC 322 (Medium): minimum number of reusable coins whose values total amount.
 * https://leetcode.com/problems/coin-change/
 *
 * Contract: non-null positive coin values; duplicates allowed. 0<=amount<MAX_INT;
 * invalid coins/amount throw IllegalArgumentException. Empty coins supported;
 * amount=0 -> 0, unreachable -> -1. Caller must budget O(amount) memory.
 * Pattern: unbounded minimum-cost DP. dp[a] is the fewest coins for a; choose the
 * last coin c and extend optimal dp[a-c]. Positive c ensures dependencies were
 * computed earlier. dp[0]=0; amount+1 is unreachable because any feasible answer
 * uses at most amount positive coins. Never add one to an unreachable sentinel.
 * O(amount * coinCount + coinCount) time, O(amount) auxiliary, O(1) output/stack;
 * pseudo-polynomial in numeric amount, not its bit length. No input mutation.
 * Greedy largest-coin selection fails for [1,3,4], amount 6.
 */
public class CoinChange {
    public int solve(int[] coins, int amount) {
        Objects.requireNonNull(coins);
        if (amount < 0 || amount == Integer.MAX_VALUE) throw new IllegalArgumentException("invalid amount");
        for (int coin : coins) if (coin <= 0) throw new IllegalArgumentException("positive coins required");
        int unreachable = amount + 1;
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, unreachable);
        dp[0] = 0;
        for (int a = 1; a <= amount; a++) {
            for (int coin : coins)
                if (coin <= a && dp[a - coin] != unreachable)
                    dp[a] = Math.min(dp[a], dp[a - coin] + 1);
        }
        return dp[amount] == unreachable ? -1 : dp[amount];
    }
}
