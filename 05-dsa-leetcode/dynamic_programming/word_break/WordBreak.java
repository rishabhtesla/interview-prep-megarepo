import java.util.*;

/**
 * LC 139 (Medium): can text be segmented entirely into reusable dictionary words?
 * https://leetcode.com/problems/word-break/
 *
 * Contract: non-null text and list of non-null NONEMPTY words. Empty entries throw
 * IllegalArgumentException; duplicates allowed. Empty text -> true, empty dictionary
 * matches only empty text. Strings compare UTF-16 units; inputs unchanged.
 * Pattern: reachable-prefix DP. dp[end] means text[0,end) has a valid segmentation.
 * For each dictionary word ending there, combine a reachable preceding prefix
 * with that word. dp[0]=true seeds the empty segmentation. Every valid segmentation
 * has a final word, so testing these cases is necessary and sufficient.
 * Uses startsWith(word,start) instead of allocating substrings in inner loops.
 * Let W=sum of dictionary word lengths (duplicates included), n=text length.
 * O(n*(W+1)+W) time upper bound, O(n) auxiliary DP, O(1) output/stack.
 * A trie can share prefix comparisons for larger dictionaries.
 */
public class WordBreak {
    public boolean solve(String text, List<String> dictionary) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(dictionary);
        for (String word : dictionary)
            if (Objects.requireNonNull(word).isEmpty()) throw new IllegalArgumentException("empty dictionary word");
        boolean[] dp = new boolean[text.length() + 1];
        dp[0] = true;
        for (int end = 1; end <= text.length(); end++) {
            for (String word : dictionary) {
                int start = end - word.length();
                if (start >= 0 && dp[start] && text.startsWith(word, start)) {
                    dp[end] = true;
                    break;
                }
            }
        }
        return dp[text.length()];
    }
}
