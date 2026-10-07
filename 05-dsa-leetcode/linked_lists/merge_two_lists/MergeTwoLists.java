/**
 * LC 21 (Easy): merge two sorted singly linked lists.
 * https://leetcode.com/problems/merge-two-sorted-lists/
 *
 * Contract: finite, acyclic, nondecreasing, NODE-DISJOINT lists; null allowed.
 * Preconditions are trusted (shared tails can create cycles when relinked).
 * Consumes/relinks input nodes; equal values from the first list precede the
 * second list's. Caller must use the returned head, not expect original chains.
 * Pattern: dummy head and growing sorted prefix. The smallest remaining node is
 * one of the two heads; appending it preserves sortedness and multiset contents.
 * Once one side ends, its counterpart's sorted suffix can be attached unchanged.
 * O(n+m) time, O(1) auxiliary/call stack, O(1) output allocation (nodes reused).
 */
public class MergeTwoLists {
    public ListNode solve(ListNode first, ListNode second) {
        ListNode dummy = new ListNode(0), tail = dummy;
        while (first != null && second != null) {
            if (first.value <= second.value) {
                tail.next = first;
                first = first.next;
            } else {
                tail.next = second;
                second = second.next;
            }
            tail = tail.next;
        }
        tail.next = first != null ? first : second;
        return dummy.next;
    }
}
