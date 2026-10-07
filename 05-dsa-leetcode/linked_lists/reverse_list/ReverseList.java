/**
 * LC 206 (Easy): reverse the direction of every link in a singly linked list.
 * https://leetcode.com/problems/reverse-linked-list/
 *
 * Contract: finite acyclic list, null allowed; uses shared public support/ListNode.
 * Relinks the original nodes in place; returns the new head (null for empty).
 * Pattern: reversed prefix plus unprocessed suffix. previous heads the correctly
 * reversed prefix, current the untouched suffix. Save current.next BEFORE rewiring
 * or the suffix becomes unreachable. Moving one node preserves the invariant;
 * when current is null the prefix includes all nodes and is the answer.
 * O(n) time, O(1) auxiliary/output/stack; output is an alias, not newly allocated.
 * Cycles are not detected: use LinkedListCycle first if the caller cannot promise
 * acyclicity. The former private-field nested node is replaced by shared ListNode.
 */
public class ReverseList {
    public ListNode solve(ListNode head) {
        ListNode previous = null;
        for (ListNode current = head; current != null;) {
            ListNode next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }
        return previous;
    }
}
