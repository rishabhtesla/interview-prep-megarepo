import java.util.*;

/**
 * LC 57 (Medium): insert one closed interval into a sorted disjoint interval list.
 * https://leetcode.com/problems/insert-interval/
 *
 * Contract: non-null arrays of [start,end], start<=end. Existing intervals must
 * be sorted and strictly disjoint (previous end < next start). Fully validated;
 * malformed order/rows throw IllegalArgumentException. No mutation or output aliases.
 * Pattern: three phases: copy strictly-before intervals, absorb every overlapping
 * interval into a growing union, then copy strictly-after intervals. Once a next
 * start exceeds union end, sorted disjoint input guarantees no later overlap.
 * Touching endpoints merge because intervals are closed. Empty input is supported.
 * O(n) time, O(1) auxiliary beyond the O(n) result/list storage, O(1) call stack.
 * Without the sorted/disjoint promise, use MergeIntervals instead.
 */
public class InsertInterval {
    public int[][] solve(int[][] intervals, int[] added) {
        Objects.requireNonNull(intervals);
        validate(added);
        for (int i = 0; i < intervals.length; i++) {
            validate(intervals[i]);
            if (i > 0 && intervals[i - 1][1] >= intervals[i][0])
                throw new IllegalArgumentException("sorted disjoint intervals required");
        }
        List<int[]> result = new ArrayList<>();
        int i = 0, start = added[0], end = added[1];
        while (i < intervals.length && intervals[i][1] < start) result.add(intervals[i++].clone());
        while (i < intervals.length && intervals[i][0] <= end) {
            start = Math.min(start, intervals[i][0]);
            end = Math.max(end, intervals[i++][1]);
        }
        result.add(new int[]{start, end});
        while (i < intervals.length) result.add(intervals[i++].clone());
        return result.toArray(int[][]::new);
    }

    private void validate(int[] interval) {
        if (interval == null || interval.length != 2 || interval[0] > interval[1])
            throw new IllegalArgumentException("invalid interval");
    }
}
