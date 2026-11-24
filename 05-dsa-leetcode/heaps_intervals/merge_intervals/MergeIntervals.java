import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * LC 56 (Medium): merge intersecting closed intervals into a disjoint sorted union.
 * https://leetcode.com/problems/merge-intervals/
 *
 * <p>Contract: non-null array of [start,end] with start<=end; malformed rows throw
 * IllegalArgumentException. Empty -> empty. Touching endpoints overlap. Input including nested rows
 * remains unchanged; output rows are fresh. Pattern: sort by start, then sweep. Completed output
 * intervals cannot overlap any later interval. Only the last output interval may overlap the next
 * start; extend its end to the maximum, or finalize it and begin another interval. Sorted starts
 * make this local decision globally complete; sorting by end instead does not establish the same
 * invariant. Comparator uses Integer.compare. O(n log(n+1)) time, O(n) auxiliary deep copy/sort
 * buffer (O(log n) sort stack), O(n) output. No arithmetic on endpoints is needed, so int extremes
 * are safe.
 */
public class MergeIntervals {
  public int[][] solve(int[][] intervals) {
    Objects.requireNonNull(intervals);
    int[][] sorted = new int[intervals.length][];
    for (int i = 0; i < intervals.length; i++) {
      int[] interval = intervals[i];
      if (interval == null || interval.length != 2 || interval[0] > interval[1]) {
        throw new IllegalArgumentException("invalid interval");
      }
      sorted[i] = interval.clone();
    }
    Arrays.sort(sorted, Comparator.comparingInt(a -> a[0]));
    List<int[]> merged = new ArrayList<>();
    for (int[] interval : sorted) {
      if (merged.isEmpty() || merged.get(merged.size() - 1)[1] < interval[0]) {
        // A sorted, non-overlapping interval can start a new output segment.
        merged.add(interval.clone());
      } else {
        // Only the last output interval can overlap the current one.
        int[] last = merged.get(merged.size() - 1);
        last[1] = Math.max(last[1], interval[1]);
      }
    }
    return merged.toArray(int[][]::new);
  }

  public static void main(String[] args) {
    int[][] intervals = {{1, 3}, {2, 6}, {8, 10}, {15, 18}};
    int[][] answer = new MergeIntervals().solve(intervals);
    int[][] expected = {{1, 6}, {8, 10}, {15, 18}};
    if (!java.util.Arrays.deepEquals(expected, answer)) {
      throw new AssertionError(
          "expected "
              + java.util.Arrays.deepToString(expected)
              + ", got "
              + java.util.Arrays.deepToString(answer));
    }
    System.out.println("MergeIntervals: " + java.util.Arrays.deepToString(answer));
  }
}
