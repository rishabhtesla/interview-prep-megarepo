import java.util.Objects;

/**
 * LC 125 (Easy): compare a string from both ends, ignoring ASCII punctuation/case.
 * https://leetcode.com/problems/valid-palindrome/
 *
 * Contract: non-null string; only ASCII letters/digits participate (all other
 * UTF-16 units are ignored). Empty or entirely ignored input is a palindrome.
 * Pattern: converging pointers. Previously consumed participating characters
 * match their mirrors. Skipping a non-participating character cannot affect the
 * normalized sequence; the next participating pair must match or no palindrome
 * is possible. Crossing pointers exhausts all required pairs.
 * O(n) time, O(1) auxiliary/output/stack; no normalized string is allocated.
 */
public class ValidPalindrome {
    public boolean solve(String text) {
        Objects.requireNonNull(text);
        int left = 0, right = text.length() - 1;
        while (left < right) {
            while (left < right && !alphanumeric(text.charAt(left))) left++;
            while (left < right && !alphanumeric(text.charAt(right))) right--;
            if (Character.toLowerCase(text.charAt(left++))
                    != Character.toLowerCase(text.charAt(right--))) return false;
        }
        return true;
    }

    private boolean alphanumeric(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
    }
}
