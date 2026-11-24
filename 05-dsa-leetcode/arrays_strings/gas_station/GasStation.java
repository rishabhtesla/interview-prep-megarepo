import java.util.Objects;

/**
 * LC 134 (Medium): find the unique circular start that completes a route.
 * https://leetcode.com/problems/gas-station/
 *
 * <p>Contract: equal nonnegative-length arrays; negative values are invalid; returns -1 when total
 * gas is insufficient. Greedy reset is valid because a failed prefix cannot be a later start. O(n)
 * time, O(1) auxiliary.
 */
public class GasStation {
  public int solve(int[] gas, int[] cost) {
    Objects.requireNonNull(gas);
    Objects.requireNonNull(cost);
    if (gas.length != cost.length) {
      throw new IllegalArgumentException("gas and cost lengths must match");
    }
    long total = 0;
    long tank = 0;
    int start = 0;
    for (int i = 0; i < gas.length; i++) {
      if (gas[i] < 0 || cost[i] < 0) {
        throw new IllegalArgumentException("gas and cost must be nonnegative");
      }
      long delta = (long) gas[i] - cost[i];
      total += delta;
      tank += delta;
      if (tank < 0) {
        // Every start through i fails: none can have more fuel than this start.
        start = i + 1;
        tank = 0;
      }
    }
    if (total < 0) {
      return -1;
    }
    return start == gas.length ? 0 : start;
  }
}
