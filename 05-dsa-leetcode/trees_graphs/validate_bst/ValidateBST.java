/**
 * LC 98 (Medium): verify the strict binary-search-tree ordering rule.
 * https://leetcode.com/problems/validate-binary-search-tree/
 *
 * Contract: finite unshared acyclic tree with any int values; null -> true.
 * Duplicate keys are INVALID. Input unchanged.
 * Pattern: inherited open bounds. Each ancestor constrains every descendant, not
 * only its immediate child. A node in (low,high) passes (low,value) to its left
 * and (value,high) to its right. These capture all ancestor restrictions.
 * Induction proves acceptance iff every subtree satisfies all inherited bounds.
 * long sentinels admit both int endpoints without +/-1 overflow.
 * O(n) time worst case, O(h) recursion stack, O(1) other auxiliary/output.
 * Recursion can overflow on very deep chains; iterative inorder is a follow-up.
 */
public class ValidateBST {
    public boolean solve(TreeNode root) {
        return valid(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    private boolean valid(TreeNode node, long low, long high) {
        if (node == null) return true;
        return low < node.value && node.value < high
                && valid(node.left, low, node.value) && valid(node.right, node.value, high);
    }
}
