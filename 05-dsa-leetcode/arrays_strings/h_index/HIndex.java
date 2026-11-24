import java.util.Arrays;
import java.util.Objects;

/**
 * LC 274 (Medium): compute the H-index from citation counts. https://leetcode.com/problems/h-index/
 *
 * <p>Contract: nonnegative counts; input unchanged. Sort-and-scan pattern. O(n log n) time, O(log
 * n) stack/auxiliary due to sorting.
 */
public class HIndex {
  public int solve(int[] citations) {
    Objects.requireNonNull(citations);
    int[] copy = citations.clone();
    for (int citation : copy) {
      if (citation < 0) {
        throw new IllegalArgumentException("citations must be nonnegative");
      }
    }
    Arrays.sort(copy);
    int h = 0;
    for (int i = 0; i < copy.length; i++) {
      // At i, exactly copy.length - i papers have at least copy[i] citations.
      int papersWithEnoughCitations = copy.length - i;
      int candidate = Math.min(copy[i], papersWithEnoughCitations);
      h = Math.max(h, candidate);
    }
    return h;
  }
}
