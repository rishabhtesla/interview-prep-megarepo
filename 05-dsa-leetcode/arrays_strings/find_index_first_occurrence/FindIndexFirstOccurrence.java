import java.util.Objects;

/**
 * LC 28 (Easy): find the first occurrence of needle in haystack.
 * https://leetcode.com/problems/find-the-index-of-the-first-occurrence-in-a-string/
 *
 * <p>Contract: null is invalid; empty needle returns 0; absent needle returns -1. Uses the standard
 * linear-time KMP prefix-function pattern, O(n+m) time and O(m) auxiliary space.
 */
public class FindIndexFirstOccurrence {
  public int solve(String haystack, String needle) {
    Objects.requireNonNull(haystack);
    Objects.requireNonNull(needle);
    if (needle.isEmpty()) {
      return 0;
    }
    int[] prefix = new int[needle.length()];
    for (int i = 1, prefixLength = 0; i < needle.length(); i++) {
      // Fall back to the next shorter border until the new character fits.
      while (prefixLength > 0 && needle.charAt(i) != needle.charAt(prefixLength)) {
        prefixLength = prefix[prefixLength - 1];
      }
      if (needle.charAt(i) == needle.charAt(prefixLength)) {
        prefixLength++;
      }
      prefix[i] = prefixLength;
    }
    for (int i = 0, matchedLength = 0; i < haystack.length(); i++) {
      // matchedLength is the prefix of needle matching the current suffix.
      while (matchedLength > 0 && haystack.charAt(i) != needle.charAt(matchedLength)) {
        matchedLength = prefix[matchedLength - 1];
      }
      if (haystack.charAt(i) == needle.charAt(matchedLength)) {
        matchedLength++;
      }
      if (matchedLength == needle.length()) {
        return i - matchedLength + 1;
      }
    }
    return -1;
  }

  public static void main(String[] args) {
    String haystack = "sadbutsad";
    String needle = "sad";
    int answer = new FindIndexFirstOccurrence().solve(haystack, needle);
    if (answer != 0) {
      throw new AssertionError("expected index 0, got " + answer);
    }
    System.out.println(
        "FindIndexFirstOccurrence: \"" + needle + "\" in \"" + haystack + "\" -> " + answer);
  }
}
