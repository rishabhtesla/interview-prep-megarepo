/**
 * LC 141 (Easy): determine whether following next links eventually revisits a node.
 * https://leetcode.com/problems/linked-list-cycle/
 *
 * Contract: null, acyclic and cyclic lists supported; comparison is NODE IDENTITY,
 * never value. Does not change links.
 * Pattern: Floyd's slow/fast pointers. An acyclic fast pointer eventually reaches
 * null. Inside a cycle of length c, fast gains one position per iteration modulo c,
 * so within c iterations it meets slow. Compare after moving to avoid an immediate
 * false hit at the initial head. Short-circuit checks protect fast.next.next.
 * O(n) time over n distinct reachable nodes, O(1) auxiliary/output/call stack.
 * Follow-up: reset one pointer to head after meeting to locate the cycle entrance.
 */
public class LinkedListCycle {
    public boolean solve(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) return true;
        }
        return false;
    }
}
