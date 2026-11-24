import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * LC 68 (Hard): fully justify words to maxWidth. https://leetcode.com/problems/text-justification/
 *
 * <p>Contract: words are non-null, nonempty, and each fits maxWidth; maxWidth is positive. The
 * final line and one-word lines are left-justified. O(total characters) time and O(total output)
 * space.
 */
public class TextJustification {
  public List<String> solve(String[] words, int maxWidth) {
    Objects.requireNonNull(words);
    if (maxWidth < 1) {
      throw new IllegalArgumentException("maxWidth must be positive");
    }
    List<String> result = new ArrayList<>();
    for (String word : words) {
      String nonNullWord = Objects.requireNonNull(word);
      if (nonNullWord.isEmpty() || nonNullWord.length() > maxWidth) {
        throw new IllegalArgumentException("word must fit maxWidth");
      }
    }

    int lineStart = 0;
    while (lineStart < words.length) {
      int lineEnd = lineStart;
      int letterCount = 0;
      while (lineEnd < words.length
          && letterCount + words[lineEnd].length() + (lineEnd - lineStart) <= maxWidth) {
        letterCount += words[lineEnd].length();
        lineEnd++;
      }
      int gapCount = lineEnd - lineStart - 1;
      boolean lastLine = lineEnd == words.length;
      int totalSpaces = maxWidth - letterCount;
      int regularSpaces = gapCount == 0 ? 0 : totalSpaces / gapCount;
      int gapsWithExtraSpace = gapCount == 0 ? 0 : totalSpaces % gapCount;
      StringBuilder line = new StringBuilder(maxWidth);

      for (int index = lineStart; index < lineEnd; index++) {
        // The first gaps receive the remainder, so every line has maxWidth chars.
        line.append(words[index]);
        if (index == lineEnd - 1) {
          break;
        }
        int spacesBetweenWords;
        if (lastLine) {
          spacesBetweenWords = 1;
        } else {
          int extra = index - lineStart < gapsWithExtraSpace ? 1 : 0;
          spacesBetweenWords = regularSpaces + extra;
        }
        line.append(" ".repeat(spacesBetweenWords));
      }

      // A final or one-word line is left-justified; pad its remaining width.
      if (lastLine || gapCount == 0) {
        line.append(" ".repeat(maxWidth - line.length()));
      }
      result.add(line.toString());
      lineStart = lineEnd;
    }
    return result;
  }
}
