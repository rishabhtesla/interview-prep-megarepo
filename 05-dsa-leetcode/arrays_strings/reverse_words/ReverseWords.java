import java.util.Objects;

/**
 * LC 151 (Medium): reverse word order and normalize spaces.
 * https://leetcode.com/problems/reverse-words-in-a-string/
 *
 * <p>Contract: words are separated by ASCII spaces; null is invalid. O(n) time and O(n)
 * output/builder space.
 */
public class ReverseWords {
  public String solve(String s) {
    Objects.requireNonNull(s);
    StringBuilder out = new StringBuilder(s.length());
    int i = s.length() - 1;
    while (i >= 0) {
      while (i >= 0 && s.charAt(i) == ' ') {
        i--;
      }
      if (i < 0) {
        break;
      }
      int wordEnd = i;
      while (i >= 0 && s.charAt(i) != ' ') {
        i--;
      }
      if (out.length() > 0) {
        out.append(' ');
      }
      // The scan discovers words from right to left, which is the output order.
      out.append(s, i + 1, wordEnd + 1);
    }
    return out.toString();
  }

  public static void main(String[] args) {
    String text = "the sky is blue";
    String answer = new ReverseWords().solve(text);
    if (!"blue is sky the".equals(answer)) {
      throw new AssertionError("expected reversed words, got " + answer);
    }
    System.out.println("ReverseWords: \"" + text + "\" -> \"" + answer + "\"");
  }
}
