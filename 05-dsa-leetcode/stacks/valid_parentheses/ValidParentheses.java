import java.util.*;

/**
 * LC 20 (Easy): decide whether brackets are correctly nested and paired.
 * https://leetcode.com/problems/valid-parentheses/
 *
 * Contract: non-null string of ()[]{}; any other character throws
 * IllegalArgumentException, even if a preceding bracket mismatch exists.
 * Empty is valid. No mutation.
 * Pattern: LIFO obligations. Push the closing bracket required by each opener.
 * A closer must satisfy the most recent unmatched opener, not an older one.
 * A mismatch is impossible to repair with later input; at the end, no obligations
 * may remain. Counts alone fail on "([)]" because they omit nesting order.
 * O(n) time, O(n) auxiliary stack, O(1) output and Java call stack.
 */
public class ValidParentheses {
    public boolean solve(String text) {
        Objects.requireNonNull(text);
        for (int i = 0; i < text.length(); i++)
            if ("()[]{}".indexOf(text.charAt(i)) < 0)
                throw new IllegalArgumentException("brackets only");
        Deque<Character> expected = new ArrayDeque<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') expected.push(')');
            else if (c == '[') expected.push(']');
            else if (c == '{') expected.push('}');
            else if (expected.isEmpty() || expected.pop() != c) return false;
        }
        return expected.isEmpty();
    }
}
