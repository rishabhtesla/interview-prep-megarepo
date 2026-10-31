import java.util.*;

/**
 * LC 68 (Hard): fully justify words to maxWidth.
 * https://leetcode.com/problems/text-justification/
 *
 * Contract: words are non-null, nonempty, and each fits maxWidth; maxWidth is
 * positive. The final line and one-word lines are left-justified. O(total
 * characters) time and O(total output) space.
 */
public class TextJustification {
    public List<String> solve(String[] words, int maxWidth) {
        Objects.requireNonNull(words); if (maxWidth < 1) throw new IllegalArgumentException();
        List<String> result = new ArrayList<>();
        for (String word : words) { Objects.requireNonNull(word); if (word.isEmpty() || word.length() > maxWidth) throw new IllegalArgumentException(); }
        int start = 0;
        while (start < words.length) {
            int end = start, letters = 0;
            while (end < words.length && letters + words[end].length() + (end - start) <= maxWidth) { letters += words[end++].length(); }
            int gaps = end - start - 1; StringBuilder line = new StringBuilder(maxWidth);
            boolean last = end == words.length;
            for (int i = start; i < end; i++) {
                line.append(words[i]);
                if (i == end - 1) break;
                int spaces = last ? 1 : (maxWidth - letters) / gaps + (i - start < (maxWidth - letters) % gaps ? 1 : 0);
                line.append(" ".repeat(spaces));
            }
            if (gaps == 0 || last) line.append(" ".repeat(maxWidth - line.length()));
            result.add(line.toString()); start = end;
        }
        return result;
    }
}
