import java.util.*;

/**
 * LC 15 (Medium): enumerate distinct value triples totaling zero.
 * https://leetcode.com/problems/3sum/
 *
 * Contract: non-null arbitrary ints; each triple uses three positions. Input is
 * copied before sorting. Output is lexicographic with ascending values per triple.
 * Pattern: fix one value then solve sorted two-sum. Duplicate anchors are skipped;
 * after a hit, equal endpoints are skipped, so each value triple is emitted once.
 * Pointer elimination is valid by sorted order. Every possible smallest element
 * is considered, giving completeness. long sums prevent overflow false positives.
 * O(n^2) time plus sorting, O(n) auxiliary copy, O(log n) sorting stack,
 * O(t) output for t triples (worst O(n^2)).
 */
public class ThreeSum {
    public List<List<Integer>> solve(int[] nums) {
        int[] sorted = Objects.requireNonNull(nums).clone();
        Arrays.sort(sorted);
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i + 2 < sorted.length; i++) {
            if (i > 0 && sorted[i] == sorted[i - 1]) continue;
            int left = i + 1, right = sorted.length - 1;
            while (left < right) {
                long sum = (long) sorted[i] + sorted[left] + sorted[right];
                if (sum < 0) left++;
                else if (sum > 0) right--;
                else {
                    result.add(List.of(sorted[i], sorted[left], sorted[right]));
                    int low = sorted[left], high = sorted[right];
                    while (left < right && sorted[left] == low) left++;
                    while (left < right && sorted[right] == high) right--;
                }
            }
        }
        return result;
    }
}
