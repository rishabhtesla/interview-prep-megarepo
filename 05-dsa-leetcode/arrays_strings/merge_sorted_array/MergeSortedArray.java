import java.util.Objects;

/**
 * LC 88 (Easy): merge two sorted arrays into nums1 in place.
 * https://leetcode.com/problems/merge-sorted-array/
 *
 * <p>Contract: m and n are valid lengths, nums1 has room for m+n values; returns nums1 and mutates
 * only its first m+n entries. Pattern: fill from the back, so no unmerged nums1 value is
 * overwritten. O(m+n) time, O(1) auxiliary.
 */
public class MergeSortedArray {
  public int[] solve(int[] nums1, int m, int[] nums2, int n) {
    Objects.requireNonNull(nums1);
    Objects.requireNonNull(nums2);
    if (m < 0
        || n < 0
        || m > nums1.length
        || n > nums2.length
        || (long) m + n > nums1.length) {
      throw new IllegalArgumentException("invalid sorted-array lengths");
    }

    int firstIndex = m - 1;
    int secondIndex = n - 1;
    int writeIndex = m + n - 1;
    while (secondIndex >= 0) {
      // Everything after writeIndex is already the correct largest suffix.
      boolean takeFirst = firstIndex >= 0 && nums1[firstIndex] > nums2[secondIndex];
      if (takeFirst) {
        nums1[writeIndex] = nums1[firstIndex];
        firstIndex--;
      } else {
        nums1[writeIndex] = nums2[secondIndex];
        secondIndex--;
      }
      writeIndex--;
    }
    return nums1;
  }
}
