import java.util.Objects;

/**
 * LC 14 (Easy): find the longest prefix shared by every string.
 * https://leetcode.com/problems/longest-common-prefix/
 *
 * <p>Contract: null array/elements are invalid; empty array returns "". O(total characters
 * inspected) time, O(1) auxiliary excluding the returned string.
 */
public class LongestCommonPrefix {
  public String solve(String[] strs) {
    Objects.requireNonNull(strs);
    if (strs.length == 0) {
      return "";
    }
    for (String value : strs) {
      Objects.requireNonNull(value);
    }
    int limit = strs[0].length();
    for (int i = 1; i < strs.length; i++) {
      limit = Math.min(limit, strs[i].length());
    }
    int i = 0;
    while (i < limit) {
      char c = strs[0].charAt(i);
      for (String value : strs) {
        if (value.charAt(i) != c) {
          return strs[0].substring(0, i);
        }
      }
      i++;
    }
    return strs[0].substring(0, limit);
  }

  public static void main(String[] args) {
    String[] words = {"flower", "flow", "flight"};
    String answer = new LongestCommonPrefix().solve(words);
    if (!"fl".equals(answer)) {
      throw new AssertionError("expected fl, got " + answer);
    }
    System.out.println(
        "LongestCommonPrefix: " + java.util.Arrays.toString(words) + " -> \"" + answer + "\"");
  }
}
