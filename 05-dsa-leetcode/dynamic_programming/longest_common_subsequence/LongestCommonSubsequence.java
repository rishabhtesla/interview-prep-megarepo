import java.util.Objects;

/**
 * LC 1143 (Medium): length of the longest sequence appearing in order in both strings.
 * https://leetcode.com/problems/longest-common-subsequence/
 *
 * Contract: non-null strings interpreted as UTF-16 units; either empty -> 0.
 * No mutation; only length returned, not the subsequence itself.
 * Pattern: prefix-pair DP, compressed to one row. Equal ending symbols extend the
 * diagonal prefix solution; otherwise an optimal solution omits at least one
 * ending symbol, giving max(previous-row same-column, current-row previous-column).
 * Before updating dp[j], it is the above cell; diagonal saves the old dp[j-1],
 * and dp[j-1] is already the left cell. Save above BEFORE overwriting it.
 * O(nm) time, O(min(n,m)) auxiliary row, O(1) output/stack. Reconstructing the
 * sequence requires additional predecessor information or Hirschberg's algorithm.
 */
public class LongestCommonSubsequence {
    public int solve(String first, String second) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        if (first.length() < second.length()) { String swap = first; first = second; second = swap; }
        int[] dp = new int[second.length() + 1];
        for (int i = 1; i <= first.length(); i++) {
            int diagonal = 0;
            for (int j = 1; j <= second.length(); j++) {
                int above = dp[j];
                dp[j] = first.charAt(i - 1) == second.charAt(j - 1)
                        ? diagonal + 1 : Math.max(above, dp[j - 1]);
                diagonal = above;
            }
        }
        return dp[second.length()];
    }
}
