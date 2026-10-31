import java.util.Objects;

/**
 * LC 26 (Easy): keep one copy of each value in a sorted array.
 * https://leetcode.com/problems/remove-duplicates-from-sorted-array/
 *
 * Contract: returns the compact-prefix length and mutates the input in place.
 * O(n) time, O(1) auxiliary.
 */
public class RemoveDuplicates {
    public int solve(int[] nums) {
        Objects.requireNonNull(nums);
        if (nums.length == 0) return 0;
        int write = 1;
        for (int read = 1; read < nums.length; read++)
            if (nums[read] != nums[write - 1]) nums[write++] = nums[read];
        return write;
    }
}
