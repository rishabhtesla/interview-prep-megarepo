import java.util.Objects;

/**
 * LC 424 (Medium): longest uppercase substring made uniform with at most k edits.
 * https://leetcode.com/problems/longest-repeating-character-replacement/
 *
 * Contract: non-null ASCII A-Z string; k >= 0; invalid inputs throw
 * IllegalArgumentException. Empty -> 0; no mutation.
 * Pattern: variable window. A window is feasible iff length - largest actual
 * frequency <= k: keep its most common character and replace everything else.
 * We recompute the actual maximum over 26 entries while shrinking, deliberately
 * avoiding the subtler stale-maximum optimization. Removing characters cannot
 * increase the required edits. The first feasible left boundary is the longest
 * feasible suffix for this right endpoint; all earlier ones are infeasible.
 * O(26n)=O(n) time (each pointer advances n times), O(26)=O(1) space/stack.
 */
public class CharacterReplacement {
    public int solve(String text, int k) {
        Objects.requireNonNull(text);
        if (k < 0) throw new IllegalArgumentException("negative budget");
        for (int i = 0; i < text.length(); i++)
            if (text.charAt(i) < 'A' || text.charAt(i) > 'Z')
                throw new IllegalArgumentException("uppercase ASCII only");
        int[] counts = new int[26];
        int left = 0, best = 0;
        for (int right = 0; right < text.length(); right++) {
            counts[text.charAt(right) - 'A']++;
            while (right - left + 1 - maximum(counts) > k) counts[text.charAt(left++) - 'A']--;
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    private int maximum(int[] counts) {
        int best = 0;
        for (int count : counts) best = Math.max(best, count);
        return best;
    }
}
