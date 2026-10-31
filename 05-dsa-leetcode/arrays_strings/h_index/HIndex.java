import java.util.Arrays;
import java.util.Objects;

/**
 * LC 274 (Medium): compute the H-index from citation counts.
 * https://leetcode.com/problems/h-index/
 *
 * Contract: nonnegative counts; input unchanged. Sort-and-scan pattern.
 * O(n log n) time, O(log n) stack/auxiliary due to sorting.
 */
public class HIndex {
    public int solve(int[] citations) {
        Objects.requireNonNull(citations);
        int[] copy = citations.clone();
        for (int c : copy) if (c < 0) throw new IllegalArgumentException();
        Arrays.sort(copy);
        int h = 0;
        for (int i = 0; i < copy.length; i++) h = Math.max(h, Math.min(copy[i], copy.length - i));
        return h;
    }
}
