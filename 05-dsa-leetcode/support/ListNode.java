/** Shared mutable singly-linked node. No structural equality: identity matters. */
public final class ListNode {
    public int value;
    public ListNode next;

    public ListNode(int value) {
        this(value, null);
    }

    public ListNode(int value, ListNode next) {
        this.value = value;
        this.next = next;
    }
}
