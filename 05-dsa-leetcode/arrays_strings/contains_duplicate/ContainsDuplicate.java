import java.util.*;

/**
 * LC 217 (Easy): does any integer appear at least twice?
 * https://leetcode.com/problems/contains-duplicate/
 *
 * Contract: non-null array, arbitrary ints; empty returns false; no mutation.
 * Pattern: membership set. Before each insertion the set holds exactly the distinct
 * processed values. A failed add witnesses a previous equal value. If every add
 * succeeds all positions were distinct. Early exit is safe after the first witness.
 * Expected O(n) time, O(n) auxiliary, O(1) result/stack; sorting trades this set for
 * O(n log n) comparisons but either mutates input or needs a copy.
 */
public class ContainsDuplicate {
    public boolean solve(int[] nums) {
        Objects.requireNonNull(nums);
        Set<Integer> seen = new HashSet<>();
        for (int value : nums) if (!seen.add(value)) return true;
        return false;
    }
}
