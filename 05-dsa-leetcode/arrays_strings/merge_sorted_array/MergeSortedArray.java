import java.util.Objects;

/**
 * LC 88 (Easy): merge two sorted arrays into nums1 in place.
 * https://leetcode.com/problems/merge-sorted-array/
 *
 * Contract: m and n are valid lengths, nums1 has room for m+n values; returns
 * nums1 and mutates only its first m+n entries. Pattern: fill from the back,
 * so no unmerged nums1 value is overwritten. O(m+n) time, O(1) auxiliary.
 */
public class MergeSortedArray {
    public int[] solve(int[] nums1, int m, int[] nums2, int n) {
        Objects.requireNonNull(nums1);
        Objects.requireNonNull(nums2);
        if (m < 0 || n < 0 || m > nums1.length || n > nums2.length
                || (long) m + n > nums1.length) throw new IllegalArgumentException();
        int i = m - 1, j = n - 1, write = m + n - 1;
        while (j >= 0) nums1[write--] = i >= 0 && nums1[i] > nums2[j] ? nums1[i--] : nums2[j--];
        return nums1;
    }
}
