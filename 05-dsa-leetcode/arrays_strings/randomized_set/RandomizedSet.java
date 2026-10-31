import java.util.*;

/**
 * LC 380 (Medium): set with average O(1) insert, remove, and random lookup.
 * https://leetcode.com/problems/insert-delete-getrandom-o1/
 *
 * Contract: values are distinct; getRandom on an empty set is invalid. Pattern:
 * array plus value-to-index map, swapping the removed tail. Expected O(1) time
 * per operation and O(n) space.
 */
public class RandomizedSet {
    private final List<Integer> values = new ArrayList<>();
    private final Map<Integer, Integer> indices = new HashMap<>();
    private final Random random;

    public RandomizedSet() { this(new Random()); }
    public RandomizedSet(Random random) { this.random = Objects.requireNonNull(random); }
    public boolean insert(int val) {
        if (indices.containsKey(val)) return false;
        indices.put(val, values.size()); values.add(val); return true;
    }
    public boolean remove(int val) {
        Integer index = indices.remove(val);
        if (index == null) return false;
        int last = values.remove(values.size() - 1);
        if (index < values.size()) { values.set(index, last); indices.put(last, index); }
        return true;
    }
    public int getRandom() {
        if (values.isEmpty()) throw new IllegalStateException();
        return values.get(random.nextInt(values.size()));
    }
}
