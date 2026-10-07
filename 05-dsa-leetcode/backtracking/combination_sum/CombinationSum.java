import java.util.*;

/**
 * LC 39 (Medium): enumerate value combinations totaling target with unlimited reuse.
 * https://leetcode.com/problems/combination-sum/
 *
 * Contract: non-null DISTINCT POSITIVE candidates, target>=0; violations throw
 * IllegalArgumentException. Empty candidates allowed; target zero -> [[]].
 * No mutation (sorts a clone). Output combinations are ascending, DFS lexicographic.
 * Pattern: reuse-current-index backtracking. Restricting next index to >= current
 * removes permutation duplicates, but recursing with i (not i+1) allows reuse.
 * Positive choices strictly reduce remaining, ensuring termination; sorted values
 * let us stop when a candidate exceeds remaining. Zero/negative choices would
 * invalidate both termination and this pruning.
 * Let D=target/minCandidate, B=visited search states, S=total output integers/lists.
 * O(n log(n+1)+nB+S) time, O(n+D) auxiliary including stack, O(S) output.
 * B <= sum(i=0..D,n^i); exponential work/deep recursion are genuine limitations.
 */
public class CombinationSum {
    public List<List<Integer>> solve(int[] candidates, int target) {
        Objects.requireNonNull(candidates);
        if (target < 0) throw new IllegalArgumentException("negative target");
        Set<Integer> seen = new HashSet<>();
        for (int value : candidates)
            if (value <= 0 || !seen.add(value)) throw new IllegalArgumentException("distinct positive candidates required");
        int[] sorted = candidates.clone();
        Arrays.sort(sorted);
        List<List<Integer>> result = new ArrayList<>();
        search(sorted, 0, target, new ArrayList<>(), result);
        return result;
    }

    private void search(int[] nums, int start, int remaining, List<Integer> path, List<List<Integer>> result) {
        if (remaining == 0) {
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = start; i < nums.length && nums[i] <= remaining; i++) {
            path.add(nums[i]);
            search(nums, i, remaining - nums[i], path, result);
            path.remove(path.size() - 1);
        }
    }
}
