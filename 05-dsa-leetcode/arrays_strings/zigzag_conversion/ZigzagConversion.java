import java.util.Objects;

/**
 * LC 6 (Medium): write a string in rows of a zigzag and read rows.
 * https://leetcode.com/problems/zigzag-conversion/
 *
 * Contract: numRows is positive; null is invalid. O(n) time and O(n) output
 * space. When rows exceed the string length, the input is unchanged.
 */
public class ZigzagConversion {
    public String solve(String s, int numRows) {
        Objects.requireNonNull(s); if (numRows < 1) throw new IllegalArgumentException();
        if (numRows == 1 || numRows >= s.length()) return s;
        StringBuilder[] rows = new StringBuilder[numRows];
        for (int i = 0; i < numRows; i++) rows[i] = new StringBuilder();
        int row = 0, direction = 1;
        for (int i = 0; i < s.length(); i++) { rows[row].append(s.charAt(i)); if (row == 0) direction = 1; else if (row == numRows - 1) direction = -1; row += direction; }
        StringBuilder out = new StringBuilder(s.length()); for (StringBuilder r : rows) out.append(r); return out.toString();
    }
}
