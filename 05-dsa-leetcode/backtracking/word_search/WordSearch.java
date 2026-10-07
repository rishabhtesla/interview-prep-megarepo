import java.util.Objects;

/**
 * LC 79 (Medium): can a word be traced through orthogonally adjacent board cells,
 * using each cell at most once in a trace?
 * https://leetcode.com/problems/word-search/
 *
 * Contract: non-null rectangular char board and non-null word; arbitrary UTF-16
 * units allowed. Null/ragged rows throw IllegalArgumentException. Empty word -> true
 * even on empty board; nonempty word on empty board -> false. Never mutates board.
 * Pattern: path-local visited backtracking. At depth index, ancestors spell exactly
 * the earlier prefix; used marks only those ancestors, not every cell ever tried.
 * Mark before exploring, unmark on BOTH success and failure. Thus alternative
 * paths can reuse a cell, but a single path cannot. Try every start for completeness.
 * For L=word length, O(rc*(1+4^L)) conservative time (after first step at most
 * three directions remain), O(rc+L) auxiliary including O(L) recursion, O(1)
 * output. Recursive depth/resource limits apply; a visited matrix avoids reserving
 * a special character that might actually appear in the board.
 */
public class WordSearch {
    public boolean solve(char[][] board, String word) {
        Objects.requireNonNull(board);
        Objects.requireNonNull(word);
        int cols = board.length == 0 ? 0 : board[0] == null ? -1 : board[0].length;
        for (char[] row : board)
            if (row == null || row.length != cols) throw new IllegalArgumentException("rectangular board required");
        if (word.isEmpty()) return true;
        if ((long) board.length * cols < word.length()) return false;
        boolean[][] used = new boolean[board.length][cols];
        for (int r = 0; r < board.length; r++)
            for (int c = 0; c < cols; c++)
                if (search(board, word, r, c, 0, used)) return true;
        return false;
    }

    private boolean search(char[][] board, String word, int r, int c, int index, boolean[][] used) {
        if (r < 0 || r >= board.length || c < 0 || c >= board[0].length
                || used[r][c] || board[r][c] != word.charAt(index)) return false;
        if (index == word.length() - 1) return true;
        used[r][c] = true;
        boolean found = search(board, word, r + 1, c, index + 1, used)
                || search(board, word, r - 1, c, index + 1, used)
                || search(board, word, r, c + 1, index + 1, used)
                || search(board, word, r, c - 1, index + 1, used);
        used[r][c] = false;
        return found;
    }
}
