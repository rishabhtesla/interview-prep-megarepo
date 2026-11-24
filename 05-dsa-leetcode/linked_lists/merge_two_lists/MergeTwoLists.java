/**
 * LC 21 (Easy): merge two sorted singly linked lists.
 * https://leetcode.com/problems/merge-two-sorted-lists/
 *
 * <p>Contract: finite, acyclic, nondecreasing, NODE-DISJOINT lists; null allowed. Preconditions are
 * trusted (shared tails can create cycles when relinked). Consumes/relinks input nodes; equal
 * values from the first list precede the second list's. Caller must use the returned head, not
 * expect original chains. Pattern: dummy head and growing sorted prefix. The smallest remaining
 * node is one of the two heads; appending it preserves sortedness and multiset contents. Once one
 * side ends, its counterpart's sorted suffix can be attached unchanged. O(n+m) time, O(1)
 * auxiliary/call stack, O(1) output allocation (nodes reused).
 */
public class MergeTwoLists {
  public ListNode solve(ListNode first, ListNode second) {
    ListNode dummy = new ListNode(0);
    ListNode tail = dummy;
    while (first != null && second != null) {
      // The smaller head is the next safe node for the sorted output prefix.
      if (first.value <= second.value) {
        tail.next = first;
        first = first.next;
      } else {
        tail.next = second;
        second = second.next;
      }
      tail = tail.next;
    }
    // The remaining list is already sorted and can be attached in one step.
    tail.next = first != null ? first : second;
    return dummy.next;
  }

  public static void main(String[] args) {
    ListNode first = new ListNode(1, new ListNode(2, new ListNode(4)));
    ListNode second = new ListNode(1, new ListNode(3, new ListNode(4)));
    ListNode merged = new MergeTwoLists().solve(first, second);
    java.util.List<Integer> values = new java.util.ArrayList<>();
    for (ListNode node = merged; node != null; node = node.next) {
      values.add(node.value);
    }
    java.util.List<Integer> expected = java.util.List.of(1, 1, 2, 3, 4, 4);
    if (!expected.equals(values)) {
      throw new AssertionError("expected " + expected + ", got " + values);
    }
    System.out.println("MergeTwoLists: -> " + values);
  }
}
