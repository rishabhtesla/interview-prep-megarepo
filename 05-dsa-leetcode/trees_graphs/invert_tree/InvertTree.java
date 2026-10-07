import java.util.*;

/**
 * LC 226 (Easy): mirror a binary tree by swapping left/right children everywhere.
 * https://leetcode.com/problems/invert-binary-tree/
 *
 * Contract: finite, unshared, acyclic tree; null -> null. Mutates original nodes;
 * returns the SAME root identity, and no tree nodes are allocated.
 * Pattern: breadth-first local transformation. Each dequeued node swaps exactly
 * once; enqueueing both children after the swap still visits the same descendants.
 * Mirroring every subtree plus exchanging their positions is precisely a mirror.
 * O(n) time, O(w) auxiliary queue for maximum level width w, O(1) output/stack.
 * Inverting twice restores all original links: useful metamorphic testing.
 */
public class InvertTree {
    public TreeNode solve(TreeNode root) {
        if (root == null) return null;
        Deque<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TreeNode node = queue.remove();
            TreeNode oldLeft = node.left;
            node.left = node.right;
            node.right = oldLeft;
            if (node.left != null) queue.add(node.left);
            if (node.right != null) queue.add(node.right);
        }
        return root;
    }
}
