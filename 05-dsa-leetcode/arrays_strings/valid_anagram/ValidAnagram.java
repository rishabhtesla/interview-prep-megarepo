import java.util.Objects;

/**
 * LC 242 (Easy): determine whether two strings have identical letter multiplicities.
 * https://leetcode.com/problems/valid-anagram/
 *
 * Contract: non-null lowercase ASCII strings, including empty; other characters
 * throw IllegalArgumentException (validated even when lengths differ). No mutation.
 * Pattern: frequency balance rather than sorting. counts[c] is the first string's
 * occurrences minus the second's; all zero is necessary and sufficient for a
 * permutation because ordering is irrelevant and every permitted symbol is counted.
 * Time O(n+m), auxiliary O(26)=O(1), output and stack O(1).
 * Follow-up: for Unicode code points, iterate codePoints() and use a map.
 */
public class ValidAnagram {
    public boolean solve(String first, String second) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        int[] counts = new int[26];
        for (int i = 0; i < first.length(); i++) {
            char c = first.charAt(i);
            requireLowercase(c);
            counts[c - 'a']++;
        }
        for (int i = 0; i < second.length(); i++) {
            char c = second.charAt(i);
            requireLowercase(c);
            counts[c - 'a']--;
        }
        for (int count : counts) if (count != 0) return false;
        return true;
    }

    private void requireLowercase(char c) {
        if (c < 'a' || c > 'z') throw new IllegalArgumentException("lowercase ASCII only");
    }
}
