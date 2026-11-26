# DSA and LeetCode interview patterns

## The answer method

For every problem, answer in this order:

```text
constraints -> brute force -> pattern -> invariant
-> implementation -> complexity -> boundary tests -> follow-up
```

Do not memorize a solution before you can say what state changes on each
iteration. The existing DSA track contains 73 standalone Java classes; every
class has a deterministic `main` example.

## Run the complete set

```bash
cd 05-dsa-leetcode
./run-examples.sh
./run-tests.sh
```

Run one class while debugging:

```bash
javac --release 17 -Xlint:all -Werror -d out \
  support/*.java trees_graphs/level_order/LevelOrder.java
java -cp out LevelOrder
```

Use the [DSA learning guide](../05-dsa-leetcode/LEARNING_GUIDE.md) for the
catalog and exact source links.

## Interview questions and answers

### How do you recognize a two-pointer problem?

**Answer:** The input has an order or boundary property that lets one pointer
move without reconsidering discarded values. In sorted two-sum, move the left
pointer up when the sum is too small and the right pointer down when it is too
large. State why the discarded region cannot contain a better answer.

### What is the sliding-window invariant?

**Answer:** Maintain a window that satisfies the current condition. Expand the
right side to include new input; while invalid, move the left side and update
counts. Record the best valid window only after restoring the invariant.

### When does binary search apply?

**Answer:** When the answer space or predicate is monotonic. The classic
invariant is: if the target exists, it remains inside the inclusive
`[low, high]` range. Check termination, midpoint overflow, duplicates, and
whether you are searching values, indexes, or an answer threshold.

### BFS versus DFS?

**Answer:** BFS explores by distance/levels and is natural for shortest
unweighted paths. DFS explores a branch and is natural for connectivity,
recursion/backtracking, and post-order dependencies. Mark visited state at the
right moment and state whether mutation of the input is allowed.

### How do you explain dynamic programming?

**Answer:** Define the smallest state that contains all information needed for
the future, write the transition, establish base cases, choose iteration order,
and state whether the table can be compressed. For `HouseRobber`, the state is
the best amount through an index; the transition chooses skip versus take.

### How do you prove a greedy algorithm?

**Answer:** State the local choice, prove an exchange or dominance argument
that an optimal solution can make that choice, then show the remaining problem
has the same structure. A passing example is not a proof that greedy is valid.

### What should you test?

**Answer:** Empty/singleton input, duplicates, already sorted/reversed data,
negative/zero values, maximum bounds, no-solution cases, and mutation/aliasing
contracts. Add a brute-force oracle for small random inputs when the optimized
algorithm is subtle.

## Pattern map

| Pattern | Runnable examples |
|---|---|
| Hash map/prefix/suffix | `arrays_strings/`, `ProductExceptSelf.java` |
| Two pointers/sliding window | `two_pointers/`, `sliding_window/` |
| Stack monotonicity | `stacks/` |
| Binary search/answer space | `binary_search/` |
| Heap/interval | `heaps_intervals/` |
| Linked list pointers | `linked_lists/` |
| BFS/DFS/union-find | `trees_graphs/` |
| Backtracking | `backtracking/` |
| Dynamic programming | `dynamic_programming/` |

## Code walkthrough example

For `MinimumWindow`, write:

```text
need counts -> expand right until all required characters exist
            -> shrink left while still valid
            -> record the shortest valid window
```

Then run its `main`, set a breakpoint at the right/left pointer updates, and
explain why shrinking cannot miss a shorter valid window that starts later.

## Revision card

```text
Constraints choose the pattern.
An invariant explains correctness.
Complexity includes auxiliary state and returned output honestly.
Mutation and index conventions are part of the contract.
Boundary tests are evidence, not decoration.
```
