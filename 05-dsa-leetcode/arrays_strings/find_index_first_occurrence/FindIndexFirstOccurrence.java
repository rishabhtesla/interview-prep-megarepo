import java.util.Objects;

/**
 * LC 28 (Easy): find the first occurrence of needle in haystack.
 * https://leetcode.com/problems/find-the-index-of-the-first-occurrence-in-a-string/
 *
 * Contract: null is invalid; empty needle returns 0; absent needle returns -1.
 * Uses the standard linear-time KMP prefix-function pattern, O(n+m) time and
 * O(m) auxiliary space.
 */
public class FindIndexFirstOccurrence {
    public int solve(String haystack, String needle) {
        Objects.requireNonNull(haystack); Objects.requireNonNull(needle);
        if (needle.isEmpty()) return 0;
        int[] prefix = new int[needle.length()];
        for (int i = 1, j = 0; i < needle.length(); i++) { while (j > 0 && needle.charAt(i) != needle.charAt(j)) j = prefix[j - 1]; if (needle.charAt(i) == needle.charAt(j)) j++; prefix[i] = j; }
        for (int i = 0, j = 0; i < haystack.length(); i++) { while (j > 0 && haystack.charAt(i) != needle.charAt(j)) j = prefix[j - 1]; if (haystack.charAt(i) == needle.charAt(j)) j++; if (j == needle.length()) return i - j + 1; }
        return -1;
    }
}
