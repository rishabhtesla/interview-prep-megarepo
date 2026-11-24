import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * LC 102 (Medium): group tree values by depth, left to right within each depth.
 * https://leetcode.com/problems/binary-tree-level-order-traversal/
 *
 * <p>Contract: finite unshared acyclic tree; null -> empty outer list; no mutation. Pattern: BFS
 * layers. Snapshot queue.size() before processing a level; those nodes all have the same depth,
 * while children appended during processing belong to the NEXT level. Left-before-right insertion
 * preserves horizontal order. Using the changing queue size as the loop limit would mix levels or
 * skip nodes. O(n) time, O(w) auxiliary queue, O(n) output values/lists, O(1) call stack. A tree
 * needs no visited set under its structural contract; a general graph does.
 */
public class LevelOrder {
  public List<List<Integer>> solve(TreeNode root) {
    List<List<Integer>> levels = new ArrayList<>();
    if (root == null) {
      return levels;
    }
    Deque<TreeNode> queue = new ArrayDeque<>();
    queue.add(root);
    while (!queue.isEmpty()) {
      int size = queue.size();
      List<Integer> level = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        TreeNode node = queue.remove();
        level.add(node.value);
        if (node.left != null) {
          queue.add(node.left);
        }
        if (node.right != null) {
          queue.add(node.right);
        }
      }
      // Children added above are deliberately reserved for the next snapshot.
      levels.add(level);
    }
    return levels;
  }

  public static void main(String[] args) {
    TreeNode root =
        new TreeNode(3, new TreeNode(9), new TreeNode(20, new TreeNode(15), new TreeNode(7)));
    java.util.List<java.util.List<Integer>> answer = new LevelOrder().solve(root);
    java.util.List<java.util.List<Integer>> expected =
        java.util.List.of(java.util.List.of(3), java.util.List.of(9, 20), java.util.List.of(15, 7));
    if (!expected.equals(answer)) {
      throw new AssertionError("expected " + expected + ", got " + answer);
    }
    System.out.println("LevelOrder: -> " + answer);
  }
}
