import java.util.*;

/**
 * LC 76 (Hard): smallest substring containing all target symbols with multiplicity.
 * https://leetcode.com/problems/minimum-window-substring/
 *
 * Contract: non-null strings interpreted as UTF-16 units. Empty target or no cover
 * -> ""; ties choose the earliest window. No mutation.
 * Pattern: deficit window. need[c] is target demand minus current-window supply
 * for target characters only; negative means surplus. missing counts unsatisfied
 * occurrences, NOT distinct letters. Adding a positive-deficit character reduces
 * missing; removing a character that becomes positive increases it.
 * While covered, shrink to enumerate the shortest cover ending here. Each left
 * endpoint is discarded only after recording its cover, so no optimum is lost.
 * Expected O(n+m) time; O(d) auxiliary for d distinct target symbols, O(w) output
 * copy of winning length w, O(1) stack. Multiplicity is essential (target "AAB").
 */
public class MinimumWindow {
    public String solve(String text, String target) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(target);
        if (target.isEmpty()) return "";
        Map<Character, Integer> need = new HashMap<>();
        for (int i = 0; i < target.length(); i++) need.merge(target.charAt(i), 1, Integer::sum);
        int missing = target.length(), left = 0, bestStart = 0, bestLength = Integer.MAX_VALUE;
        for (int right = 0; right < text.length(); right++) {
            char added = text.charAt(right);
            if (need.containsKey(added)) {
                if (need.get(added) > 0) missing--;
                need.put(added, need.get(added) - 1);
            }
            while (missing == 0) {
                if (right - left + 1 < bestLength) {
                    bestStart = left;
                    bestLength = right - left + 1;
                }
                char removed = text.charAt(left++);
                if (need.containsKey(removed)) {
                    need.put(removed, need.get(removed) + 1);
                    if (need.get(removed) > 0) missing++;
                }
            }
        }
        return bestLength == Integer.MAX_VALUE ? "" : text.substring(bestStart, bestStart + bestLength);
    }
}
