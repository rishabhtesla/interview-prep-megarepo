import java.util.Objects;

/**
 * LC 238 (Medium): at each position multiply every other element, without division.
 * https://leetcode.com/problems/product-of-array-except-self/
 *
 * Contract: non-null ints; empty -> empty, singleton -> [1]. All intermediate
 * prefix/suffix/output products must fit long; overflow throws ArithmeticException.
 * Returns long[] and leaves input unchanged. Zeros need no special branch.
 * Pattern: prefix/suffix decomposition. First pass writes product strictly left
 * of i. Second pass multiplies product strictly right of i. Those two disjoint
 * regions contain every required factor exactly once, proving the result.
 * O(n) time, O(1) auxiliary/stack, O(n) output. Avoid calculating unused full
 * products: they can overflow even when every requested answer is representable.
 */
public class ProductExceptSelf {
    public long[] solve(int[] nums) {
        Objects.requireNonNull(nums);
        long[] result = new long[nums.length];
        long prefix = 1;
        for (int i = 0; i < nums.length; i++) {
            result[i] = prefix;
            if (i + 1 < nums.length) prefix = Math.multiplyExact(prefix, nums[i]);
        }
        long suffix = 1;
        for (int i = nums.length - 1; i >= 0; i--) {
            result[i] = Math.multiplyExact(result[i], suffix);
            if (i > 0) suffix = Math.multiplyExact(suffix, nums[i]);
        }
        return result;
    }
}
