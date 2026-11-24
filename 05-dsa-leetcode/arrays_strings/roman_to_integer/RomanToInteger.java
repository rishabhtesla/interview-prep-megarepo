import java.util.Map;
import java.util.Objects;

/**
 * LC 13 (Easy): convert a valid Roman numeral to an integer.
 * https://leetcode.com/problems/roman-to-integer/
 *
 * <p>Contract: non-null, non-empty canonical Roman numeral; invalid symbols or non-canonical
 * spelling throws IllegalArgumentException. O(n) time, O(1) space.
 */
public class RomanToInteger {
  public int solve(String s) {
    Objects.requireNonNull(s);
    if (s.isEmpty()) {
      throw new IllegalArgumentException("Roman numeral cannot be empty");
    }
    Map<Character, Integer> values =
        Map.of('I', 1, 'V', 5, 'X', 10, 'L', 50, 'C', 100, 'D', 500, 'M', 1000);
    int total = 0;
    for (int i = 0; i < s.length(); i++) {
      Integer currentValue = values.get(s.charAt(i));
      if (currentValue == null) {
        throw new IllegalArgumentException("unknown Roman symbol");
      }
      int nextValue =
          i + 1 < s.length() ? values.getOrDefault(s.charAt(i + 1), -1) : 0;
      if (nextValue < 0) {
        throw new IllegalArgumentException("unknown Roman symbol");
      }

      // A smaller value before a larger value is subtractive (IV, IX, and so on).
      total += nextValue > currentValue ? -currentValue : currentValue;
    }
    if (!new IntegerToRoman().solve(total).equals(s)) {
      // The conversion also rejects illegal subtractive pairs and repetitions.
      throw new IllegalArgumentException("non-canonical Roman numeral");
    }
    return total;
  }
}
