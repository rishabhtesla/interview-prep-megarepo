import java.util.Objects;

/**
 * LC 189 (Medium): rotate an array right by k steps in place.
 * https://leetcode.com/problems/rotate-array/
 *
 * Contract: negative k is invalid; input is mutated and returned. Pattern:
 * reverse whole array, then each block. O(n) time, O(1) auxiliary.
 */
public class RotateArray {
    public int[] solve(int[] nums, int k) {
        Objects.requireNonNull(nums);
        if (k < 0) throw new IllegalArgumentException();
        if (nums.length == 0) return nums;
        k %= nums.length;
        reverse(nums, 0, nums.length - 1);
        reverse(nums, 0, k - 1);
        reverse(nums, k, nums.length - 1);
        return nums;
    }
    private void reverse(int[] a, int lo, int hi) {
        while (lo < hi) { int t = a[lo++]; a[lo - 1] = a[hi]; a[hi--] = t; }
    }
}
