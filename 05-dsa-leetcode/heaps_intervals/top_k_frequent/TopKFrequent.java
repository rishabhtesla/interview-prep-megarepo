import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * LC 347 (Medium): return the k most frequent distinct values.
 * https://leetcode.com/problems/top-k-frequent-elements/
 *
 * <p>Contract: non-null ints; 0<=k<=distinct count, otherwise IllegalArgumentException.
 * Deterministic result: decreasing frequency, then increasing value. k=0 -> empty. No input
 * mutation. Pattern: count then bounded heap. Its root is the WORST retained candidate: smallest
 * frequency, then largest value. Evicting the worst maintains the best k seen candidates. Draining
 * worst-to-best into reverse indices yields required output order. Integer.compare avoids
 * comparator overflow at extreme values. Expected O(n+d log(k+1)) time, O(d+k) auxiliary map/heap,
 * O(k) output, O(1) call stack. Bucket sorting is an O(n) alternative with O(n) buckets.
 */
public class TopKFrequent {
  public int[] solve(int[] nums, int k) {
    Objects.requireNonNull(nums);
    Map<Integer, Integer> frequency = new HashMap<>();
    for (int value : nums) {
      frequency.merge(value, 1, Integer::sum);
    }
    if (k < 0 || k > frequency.size()) {
      throw new IllegalArgumentException("invalid count");
    }
    if (k == 0) {
      return new int[0];
    }
    Comparator<Integer> worstFirst =
        (a, b) -> {
          int compare = Integer.compare(frequency.get(a), frequency.get(b));
          return compare != 0 ? compare : Integer.compare(b, a);
        };
    PriorityQueue<Integer> heap = new PriorityQueue<>(worstFirst);
    for (int value : frequency.keySet()) {
      heap.add(value);
      if (heap.size() > k) {
        // Root is the worst retained candidate according to worstFirst.
        heap.remove();
      }
    }
    int[] result = new int[k];
    for (int i = k - 1; i >= 0; i--) {
      result[i] = heap.remove();
    }
    return result;
  }

  public static void main(String[] args) {
    int[] nums = {1, 1, 1, 2, 2, 3};
    int[] answer = new TopKFrequent().solve(nums, 2);
    java.util.Arrays.sort(answer);
    int[] expected = {1, 2};
    if (!java.util.Arrays.equals(expected, answer)) {
      throw new AssertionError(
          "expected "
              + java.util.Arrays.toString(expected)
              + ", got "
              + java.util.Arrays.toString(answer));
    }
    System.out.println("TopKFrequent: k=2 -> " + java.util.Arrays.toString(answer));
  }
}
