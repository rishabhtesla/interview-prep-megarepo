import java.util.Objects;

/**
 * LC 151 (Medium): reverse word order and normalize spaces.
 * https://leetcode.com/problems/reverse-words-in-a-string/
 *
 * Contract: words are separated by ASCII spaces; null is invalid. O(n) time
 * and O(n) output/builder space.
 */
public class ReverseWords {
    public String solve(String s) {
        Objects.requireNonNull(s);
        StringBuilder out = new StringBuilder(s.length());
        int i = s.length() - 1;
        while (i >= 0) {
            while (i >= 0 && s.charAt(i) == ' ') i--;
            if (i < 0) break;
            int end = i;
            while (i >= 0 && s.charAt(i) != ' ') i--;
            if (out.length() > 0) out.append(' ');
            out.append(s, i + 1, end + 1);
        }
        return out.toString();
    }
}
