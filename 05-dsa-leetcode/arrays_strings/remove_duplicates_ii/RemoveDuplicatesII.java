import java.util.Objects;

/**
 * LC 80 (Medium): keep at most two copies in a sorted array.
 * https://leetcode.com/problems/remove-duplicates-from-sorted-array-ii/
 *
 * Contract: returns the compact-prefix length and mutates the input in place.
 * O(n) time, O(1) auxiliary.
 */
public class RemoveDuplicatesII {
    public int solve(int[] nums) {
        Objects.requireNonNull(nums);
        int write = 0;
        for (int num : nums)
            if (write < 2 || num != nums[write - 2]) nums[write++] = num;
        return write;
    }
}
