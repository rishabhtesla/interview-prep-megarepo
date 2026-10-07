/**
 * LC 235 (Medium): find the deepest BST node ancestral to two specified keys.
 * https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-search-tree/
 *
 * Contract: finite strict BST with unique int keys (trusted). Keys, rather than
 * node references, identify targets. Returns null if either is absent; equal keys
 * are allowed and return that node. A node is its own ancestor. No mutation.
 * Pattern: ordered descent to a split. If both keys are smaller/larger, every
 * common ancestor below this node must lie in that child. At the first split
 * (or equality), no child contains both, making this node the lowest ancestor.
 * Explicit membership searches prevent returning a plausible split for missing keys.
 * O(h) time including membership checks, O(1) auxiliary/output/stack. In a
 * skewed BST h=n; the algorithm is not universally O(log n).
 */
public class LowestCommonAncestorBST {
    public TreeNode solve(TreeNode root, int first, int second) {
        if (!contains(root, first) || !contains(root, second)) return null;
        int low = Math.min(first, second), high = Math.max(first, second);
        while (root != null) {
            if (root.value > high) root = root.left;
            else if (root.value < low) root = root.right;
            else return root;
        }
        return null;
    }

    private boolean contains(TreeNode root, int key) {
        while (root != null) {
            if (root.value == key) return true;
            root = key < root.value ? root.left : root.right;
        }
        return false;
    }
}
