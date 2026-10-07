import java.util.*;

/**
 * LC 49 (Medium): partition words by rearrangement-equivalence.
 * https://leetcode.com/problems/group-anagrams/
 *
 * Contract: non-null array of non-null strings; UTF-16 code units define symbols.
 * Empty input yields no groups; empty words group together. Does not mutate input.
 * Pattern: canonical key. Sorted character sequences are equal iff multiplicities
 * are equal, so each key identifies exactly one equivalence class. LinkedHashMap
 * makes group order deterministic by first appearance; word order is preserved.
 * For total characters C and maximum word length L: O(C log(max(2,L)) + n) time.
 * O(C+n) key/map auxiliary, O(n) output references, O(L) transient sorting storage;
 * library sorting may use O(log L) stack. Strings themselves are not duplicated.
 * Follow-up: 26-count keys avoid sorting when the alphabet is lowercase ASCII.
 */
public class GroupAnagrams {
    public List<List<String>> solve(String[] words) {
        Objects.requireNonNull(words);
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String word : words) {
            char[] letters = Objects.requireNonNull(word).toCharArray();
            Arrays.sort(letters);
            groups.computeIfAbsent(new String(letters), k -> new ArrayList<>()).add(word);
        }
        return new ArrayList<>(groups.values());
    }
}
