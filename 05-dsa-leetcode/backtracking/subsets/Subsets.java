import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * LC 78 (Medium): enumerate every subset of distinct input values.
 * https://leetcode.com/problems/subsets/
 *
 * <p>Contract: non-null distinct ints; duplicates throw IllegalArgumentException. Empty -> [[]]. No
 * mutation. Subset element order follows input order; output order is deterministic DFS. Practical
 * inputs must allow exponential output. Pattern: increasing-choice backtracking. path contains only
 * chosen indices smaller than start. Every subset has exactly one increasing index sequence, giving
 * uniqueness and completeness. Emit a COPY at each node; retaining the same mutable path would make
 * every result change when backtracking. Undo the last choice after recursion to restore the
 * caller's invariant. O(n*2^n) time including copies, O(n) auxiliary set/path/recursion stack,
 * O(n*2^n) output (plus one empty list when n=0).
 */
public class Subsets {
  public List<List<Integer>> solve(int[] nums) {
    Objects.requireNonNull(nums);
    Set<Integer> seen = new HashSet<>();
    for (int value : nums) {
      if (!seen.add(value)) {
        throw new IllegalArgumentException("distinct values required");
      }
    }
    List<List<Integer>> result = new ArrayList<>();
    search(nums, 0, new ArrayList<>(), result);
    return result;
  }

  private void search(int[] nums, int start, List<Integer> path, List<List<Integer>> result) {
    // Every recursion node is already one valid subset, including the empty one.
    result.add(new ArrayList<>(path));
    for (int i = start; i < nums.length; i++) {
      path.add(nums[i]);
      search(nums, i + 1, path, result);
      // Remove only the choice made by this stack frame.
      path.remove(path.size() - 1);
    }
  }

  public static void main(String[] args) {
    int[] nums = {1, 2, 3};
    java.util.List<java.util.List<Integer>> answer = new Subsets().solve(nums);
    if (answer.size() != 8
        || !answer.contains(java.util.List.of(1, 2, 3))
        || !answer.contains(java.util.List.of())) {
      throw new AssertionError("unexpected subsets: " + answer);
    }
    System.out.println("Subsets: " + java.util.Arrays.toString(nums) + " -> " + answer);
  }
}
