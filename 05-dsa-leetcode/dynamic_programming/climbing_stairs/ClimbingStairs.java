/**
 * LC 70 (Easy): count ordered ways to climb n steps with moves of one or two.
 * https://leetcode.com/problems/climbing-stairs/
 *
 * Contract: 0<=n<=91, otherwise IllegalArgumentException. Returns long; n=0 has
 * ONE empty sequence, n=1 one way. 91 is the last n whose answer fits signed long.
 * Pattern: rolling DP. Every nonempty route ends in a one-step or two-step move,
 * disjoint cases with ways[n-1] and ways[n-2] possibilities. Base ways[0]=ways[1]=1
 * gives the recurrence. Before each iteration twoBack and oneBack hold exactly
 * those two dependencies; update only after computing their sum.
 * O(n) time, O(1) auxiliary/output/stack. Naive recursion recomputes overlapping
 * subproblems exponentially; rolling state removes the unnecessary DP array.
 */
public class ClimbingStairs {
    public long solve(int n) {
        if (n < 0 || n > 91) throw new IllegalArgumentException("n must be in 0..91");
        long twoBack = 1, oneBack = 1;
        for (int step = 2; step <= n; step++) {
            long current = twoBack + oneBack;
            twoBack = oneBack;
            oneBack = current;
        }
        return oneBack;
    }
}
