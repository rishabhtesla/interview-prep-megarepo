import java.util.*;

/**
 * LC 3 (Medium): longest substring with no repeated UTF-16 code unit.
 * https://leetcode.com/problems/longest-substring-without-repeating-characters/
 *
 * Contract: non-null string; empty -> 0; supplementary Unicode characters occupy
 * two code units, not one code point. No mutation.
 * Pattern: last-seen sliding window. [left,right] is duplicate-free after moving
 * left beyond the current symbol's previous occurrence. Never move left backward:
 * an old occurrence outside the window is irrelevant (try "abba").
 * This is the longest valid suffix ending at right, so maximizing over right
 * considers an optimal window. Expected O(n) time, O(min(n,65536)) auxiliary map,
 * O(1) output/stack. A set plus repeated shrinking is another linear solution.
 */
public class LongestSubstring {
    public int solve(String text) {
        Objects.requireNonNull(text);
        Map<Character, Integer> last = new HashMap<>();
        int left = 0, best = 0;
        for (int right = 0; right < text.length(); right++) {
            char c = text.charAt(right);
            Integer previous = last.put(c, right);
            if (previous != null) left = Math.max(left, previous + 1);
            best = Math.max(best, right - left + 1);
        }
        return best;
    }
}
