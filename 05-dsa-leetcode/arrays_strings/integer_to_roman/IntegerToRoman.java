/**
 * LC 12 (Medium): convert an integer to its canonical Roman numeral.
 * https://leetcode.com/problems/integer-to-roman/
 *
 * <p>Contract: input is 1..3999. Greedy value/symbol pairs produce canonical subtractive notation.
 * O(1) time and O(1) output-table space.
 */
public class IntegerToRoman {
  public String solve(int num) {
    if (num < 1 || num > 3999) {
      throw new IllegalArgumentException("Roman numerals support 1..3999");
    }
    int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < values.length; i++) {
      while (num >= values[i]) {
        // Taking the largest valid symbol first preserves canonical notation.
        out.append(symbols[i]);
        num -= values[i];
      }
    }
    return out.toString();
  }
}
