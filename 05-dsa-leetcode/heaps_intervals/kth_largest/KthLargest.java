import java.util.*;

/**
 * LC 215 (Medium): kth largest array entry, counting duplicate occurrences.
 * https://leetcode.com/problems/kth-largest-element-in-an-array/
 *
 * Contract: non-null int array; 1<=k<=n, otherwise IllegalArgumentException.
 * Input unchanged; this is rank, NOT kth distinct value.
 * Pattern: bounded min-heap. After each prefix the heap holds its largest k
 * elements (or the whole prefix if smaller). Adding an element and evicting
 * the minimum when oversized restores that invariant by removing the only
 * candidate that cannot belong in the largest k. Its final minimum is rank k.
 * O(n log(k+1)) time, O(k) auxiliary, O(1) output/call stack.
 * Quickselect gives expected linear time but usually mutates its working array.
 */
public class KthLargest {
    public int solve(int[] nums, int k) {
        Objects.requireNonNull(nums);
        if (k < 1 || k > nums.length) throw new IllegalArgumentException("invalid rank");
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        for (int value : nums) {
            heap.add(value);
            if (heap.size() > k) heap.remove();
        }
        return heap.element();
    }
}
