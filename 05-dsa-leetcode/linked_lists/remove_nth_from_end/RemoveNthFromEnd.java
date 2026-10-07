/**
 * LC 19 (Medium): remove the node n positions from the end, counting from one.
 * https://leetcode.com/problems/remove-nth-node-from-end-of-list/
 *
 * Contract: finite acyclic list. Requires 1 <= n <= length; invalid n (including
 * any n for null head) throws IllegalArgumentException WITHOUT changing links.
 * Pattern: fixed-gap pointers from a dummy predecessor. Advance fast n steps;
 * then move both until fast is last. slow is now directly before the removal:
 * exactly n nodes lie after slow, so its next is nth from the end.
 * Dummy unifies deleting the head with interior deletion. Only after validating
 * the gap is any input link rewritten. Removed node's own next is left intact.
 * O(length) time, O(1) auxiliary/output/call stack; reuses remaining nodes.
 */
public class RemoveNthFromEnd {
    public ListNode solve(ListNode head, int n) {
        if (n <= 0) throw new IllegalArgumentException("n must be positive");
        ListNode dummy = new ListNode(0, head), fast = dummy, slow = dummy;
        for (int i = 0; i < n; i++) {
            if (fast.next == null) throw new IllegalArgumentException("n exceeds length");
            fast = fast.next;
        }
        while (fast.next != null) {
            fast = fast.next;
            slow = slow.next;
        }
        slow.next = slow.next.next;
        return dummy.next;
    }
}
