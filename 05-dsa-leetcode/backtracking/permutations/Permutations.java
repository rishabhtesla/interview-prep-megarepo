import java.util.*;

/**
 * LC 46 (Medium): enumerate all orderings of distinct values.
 * https://leetcode.com/problems/permutations/
 *
 * Contract: non-null distinct ints, duplicates throw IllegalArgumentException;
 * empty -> [[]] (one empty permutation). No mutation; deterministic input-order DFS.
 * Pattern: used-position backtracking. path is a prefix with no reused index;
 * used marks exactly its elements. Each unused position is a possible next choice.
 * At length n the prefix is a full permutation. Every ordering has one unique
 * sequence of choices, proving completeness/no duplicates. Undo both used and path.
 * O(n*n!) time including leaf copies, O(n) auxiliary path/flags/set/stack,
 * O(n*n!) output; factorial growth makes this suitable only for small n.
 * With duplicate values, sorted sibling-skipping is needed instead of this contract.
 */
public class Permutations {
    public List<List<Integer>> solve(int[] nums) {
        Objects.requireNonNull(nums);
        Set<Integer> seen = new HashSet<>();
        for (int value : nums) if (!seen.add(value)) throw new IllegalArgumentException("distinct values required");
        List<List<Integer>> result = new ArrayList<>();
        search(nums, new boolean[nums.length], new ArrayList<>(), result);
        return result;
    }

    private void search(int[] nums, boolean[] used, List<Integer> path, List<List<Integer>> result) {
        if (path.size() == nums.length) {
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = 0; i < nums.length; i++) {
            if (used[i]) continue;
            used[i] = true;
            path.add(nums[i]);
            search(nums, used, path, result);
            path.remove(path.size() - 1);
            used[i] = false;
        }
    }
}
