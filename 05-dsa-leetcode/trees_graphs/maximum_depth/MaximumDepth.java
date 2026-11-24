/**
 * LC 104 (Easy): maximum number of nodes on a root-to-leaf path.
 * https://leetcode.com/problems/maximum-depth-of-binary-tree/
 *
 * <p>Contract: finite, unshared, acyclic tree; null -> 0; input unchanged. Pattern: postorder
 * structural recursion. Empty depth is zero; any nonempty path contains the root followed by a path
 * in exactly one child. Thus depth(node)=1+max(depth(left),depth(right)). Induction on subtree size
 * proves correctness because the recursive answers are computed before their parent. O(n) time,
 * O(h) recursion stack, O(1) non-stack auxiliary/output. h is O(log n) only for balanced trees; a
 * chain uses O(n) stack and can overflow the JVM stack. An explicit DFS stack/BFS queue is the
 * iterative follow-up.
 */
public class MaximumDepth {
  public int solve(TreeNode root) {
    if (root == null) {
      return 0;
    }
    // The root contributes one node, followed by the deeper child path.
    return 1 + Math.max(solve(root.left), solve(root.right));
  }

  public static void main(String[] args) {
    TreeNode root =
        new TreeNode(3, new TreeNode(9), new TreeNode(20, new TreeNode(15), new TreeNode(7)));
    int answer = new MaximumDepth().solve(root);
    if (answer != 3) {
      throw new AssertionError("expected depth 3, got " + answer);
    }
    System.out.println("MaximumDepth: -> " + answer);
  }
}
