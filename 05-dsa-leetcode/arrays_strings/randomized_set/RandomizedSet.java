import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * LC 380 (Medium): set with average O(1) insert, remove, and random lookup.
 * https://leetcode.com/problems/insert-delete-getrandom-o1/
 *
 * <p>Contract: values are distinct; getRandom on an empty set is invalid. Pattern: array plus
 * value-to-index map, swapping the removed tail. Expected O(1) time per operation and O(n) space.
 */
public class RandomizedSet {
  private final List<Integer> values = new ArrayList<>();
  private final Map<Integer, Integer> indices = new HashMap<>();
  private final Random random;

  public RandomizedSet() {
    this(new Random());
  }

  public RandomizedSet(Random random) {
    this.random = Objects.requireNonNull(random);
  }

  public boolean insert(int val) {
    if (indices.containsKey(val)) {
      return false;
    }
    indices.put(val, values.size());
    values.add(val);
    return true;
  }

  public boolean remove(int val) {
    Integer index = indices.remove(val);
    if (index == null) {
      return false;
    }

    // Move the tail into the hole so removal does not shift every later value.
    int lastIndex = values.size() - 1;
    int lastValue = values.remove(lastIndex);
    if (index < values.size()) {
      values.set(index, lastValue);
      indices.put(lastValue, index);
    }
    return true;
  }

  public int getRandom() {
    if (values.isEmpty()) {
      throw new IllegalStateException("cannot choose from an empty set");
    }
    return values.get(random.nextInt(values.size()));
  }

  public static void main(String[] args) {
    RandomizedSet set = new RandomizedSet(new java.util.Random(0));
    boolean insertedOne = set.insert(1);
    boolean insertedDuplicate = set.insert(1);
    boolean removedOne = set.remove(1);
    boolean insertedTwo = set.insert(2);
    int randomValue = set.getRandom();
    if (!insertedOne || insertedDuplicate || !removedOne || !insertedTwo || randomValue != 2) {
      throw new AssertionError("unexpected operation results");
    }
    System.out.println(
        "RandomizedSet: insert(1), duplicate insert, remove(1), insert(2), getRandom() -> "
            + randomValue);
  }
}
