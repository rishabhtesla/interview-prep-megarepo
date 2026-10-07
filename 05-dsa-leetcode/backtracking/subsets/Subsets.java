import java.util.*;

/**
 * LC 78 (Medium): enumerate every subset of distinct input values.
 * https://leetcode.com/problems/subsets/
 *
 * Contract: non-null distinct ints; duplicates throw IllegalArgumentException.
 * Empty -> [[]]. No mutation. Subset element order follows input order; output
 * order is deterministic DFS. Practical inputs must allow exponential output.
 * Pattern: increasing-choice backtracking. path contains only chosen indices
 * smaller than start. Every subset has exactly one increasing index sequence,
 * giving uniqueness and completeness. Emit a COPY at each node; retaining the
 * same mutable path would make every result change when backtracking.
 * Undo the last choice after recursion to restore the caller's invariant.
 * O(n*2^n) time including copies, O(n) auxiliary set/path/recursion stack,
 * O(n*2^n) output (plus one empty list when n=0).
 */
public class Subsets {
    public List<List<Integer>> solve(int[] nums) {
        Objects.requireNonNull(nums);
        Set<Integer> seen = new HashSet<>();
        for (int value : nums) if (!seen.add(value)) throw new IllegalArgumentException("distinct values required");
        List<List<Integer>> result = new ArrayList<>();
        search(nums, 0, new ArrayList<>(), result);
        return result;
    }

    private void search(int[] nums, int start, List<Integer> path, List<List<Integer>> result) {
        result.add(new ArrayList<>(path));
        for (int i = start; i < nums.length; i++) {
            path.add(nums[i]);
            search(nums, i + 1, path, result);
            path.remove(path.size() - 1);
        }
    }
}
