import java.util.*;

/**
 * LC 13 (Easy): convert a valid Roman numeral to an integer.
 * https://leetcode.com/problems/roman-to-integer/
 *
 * Contract: non-null, non-empty canonical Roman numeral; invalid symbols or
 * non-canonical spelling throws IllegalArgumentException. O(n) time, O(1) space.
 */
public class RomanToInteger {
    public int solve(String s) {
        Objects.requireNonNull(s); if (s.isEmpty()) throw new IllegalArgumentException();
        Map<Character, Integer> v = Map.of('I', 1, 'V', 5, 'X', 10, 'L', 50, 'C', 100, 'D', 500, 'M', 1000);
        int total = 0;
        for (int i = 0; i < s.length(); i++) { Integer cur = v.get(s.charAt(i)); if (cur == null) throw new IllegalArgumentException(); int next = i + 1 < s.length() ? v.getOrDefault(s.charAt(i + 1), -1) : 0; if (next < 0) throw new IllegalArgumentException(); total += next > cur ? -cur : cur; }
        if (!new IntegerToRoman().solve(total).equals(s)) throw new IllegalArgumentException();
        return total;
    }
}
