import java.util.Objects;

/**
 * LC 58 (Easy): return the length of the final word.
 * https://leetcode.com/problems/length-of-last-word/
 *
 * <p>Contract: null is invalid; whitespace is ASCII space and an all-space string returns 0. Scan
 * backwards. O(n) time, O(1) auxiliary.
 */
public class LengthOfLastWord {
  public int solve(String s) {
    Objects.requireNonNull(s);
    int i = s.length() - 1;
    // First skip trailing separators so i can land on the final word.
    while (i >= 0 && s.charAt(i) == ' ') {
      i--;
    }
    int end = i;
    // Then scan to the word's left boundary. The inclusive length is end - i.
    while (i >= 0 && s.charAt(i) != ' ') {
      i--;
    }
    return end - i;
  }
}
