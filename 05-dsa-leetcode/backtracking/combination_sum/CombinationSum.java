import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * LC 39 (Medium): enumerate value combinations totaling target with unlimited reuse.
 * https://leetcode.com/problems/combination-sum/
 *
 * <p>Contract: non-null DISTINCT POSITIVE candidates, target>=0; violations throw
 * IllegalArgumentException. Empty candidates allowed; target zero -> [[]]. No mutation (sorts a
 * clone). Output combinations are ascending, DFS lexicographic. Pattern: reuse-current-index
 * backtracking. Restricting next index to >= current removes permutation duplicates, but recursing
 * with i (not i+1) allows reuse. Positive choices strictly reduce remaining, ensuring termination;
 * sorted values let us stop when a candidate exceeds remaining. Zero/negative choices would
 * invalidate both termination and this pruning. Let D=target/minCandidate, B=visited search states,
 * S=total output integers/lists. O(n log(n+1)+nB+S) time, O(n+D) auxiliary including stack, O(S)
 * output. B <= sum(i=0..D,n^i); exponential work/deep recursion are genuine limitations.
 */
public class CombinationSum {
  public List<List<Integer>> solve(int[] candidates, int target) {
    Objects.requireNonNull(candidates);
    if (target < 0) {
      throw new IllegalArgumentException("negative target");
    }
    Set<Integer> seen = new HashSet<>();
    for (int value : candidates) {
      if (value <= 0 || !seen.add(value)) {
        throw new IllegalArgumentException("distinct positive candidates required");
      }
    }
    int[] sorted = candidates.clone();
    Arrays.sort(sorted);
    List<List<Integer>> result = new ArrayList<>();
    search(sorted, 0, target, new ArrayList<>(), result);
    return result;
  }

  private void search(
      int[] nums, int start, int remaining, List<Integer> path, List<List<Integer>> result) {
    if (remaining == 0) {
      result.add(new ArrayList<>(path));
      return;
    }
    for (int i = start; i < nums.length && nums[i] <= remaining; i++) {
      // Reusing i is allowed; increasing the next index would solve combinations
      // without repetition instead.
      path.add(nums[i]);
      search(nums, i, remaining - nums[i], path, result);
      // Restore the prefix invariant before trying the next candidate.
      path.remove(path.size() - 1);
    }
  }

  public static void main(String[] args) {
    int[] candidates = {2, 3, 6, 7};
    java.util.List<java.util.List<Integer>> answer = new CombinationSum().solve(candidates, 7);
    java.util.List<java.util.List<Integer>> expected =
        java.util.List.of(java.util.List.of(2, 2, 3), java.util.List.of(7));
    if (!expected.equals(answer)) {
      throw new AssertionError("expected " + expected + ", got " + answer);
    }
    System.out.println("CombinationSum: target=7 -> " + answer);
  }
}
