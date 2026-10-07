/**
 * LC 543 (Easy): maximum number of EDGES on any path in a binary tree.
 * https://leetcode.com/problems/diameter-of-binary-tree/
 *
 * Contract: finite unshared acyclic tree; empty/singleton -> 0; no mutation.
 * Pattern: postorder with a global aggregate local to each invocation.
 * height returns nodes on a downward path; leftHeight+rightHeight equals edges
 * on the longest path whose highest node is current. Every tree path has one
 * highest node, so taking the maximum over all nodes includes the optimal path.
 * Return only the larger downward branch to parents: a path cannot fork twice.
 * O(n) time, O(h) recursion stack, O(1) aggregate/output; deep chains may exhaust
 * JVM stack. No instance field means repeated calls cannot leak an old diameter.
 */
public class Diameter {
    public int solve(TreeNode root) {
        int[] best = {0};
        height(root, best);
        return best[0];
    }

    private int height(TreeNode node, int[] best) {
        if (node == null) return 0;
        int left = height(node.left, best), right = height(node.right, best);
        best[0] = Math.max(best[0], left + right);
        return 1 + Math.max(left, right);
    }
}
