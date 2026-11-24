# DSA learning guide: pattern first, problem second

Do not read all solutions from top to bottom. Use the progression below and
solve the exercise before opening the implementation.

## The seven-step method for every problem

1. Restate the input, output, and constraints.
2. Write a brute-force approach and its complexity.
3. Identify the pattern.
4. State an invariant in one sentence.
5. Trace the example by hand.
6. Implement and run the test.
7. Explain a boundary case and a follow-up.

For example, binary search uses this invariant:

```text
If the target exists, it is inside the inclusive range [low, high].
```

## Progression

| Stage | Directory | What to master |
|---:|---|---|
| 1 | `arrays_strings/` | Arrays, strings, maps, prefix/suffix, greedy, compaction |
| 2 | `two_pointers/`, `sliding_window/` | Monotonic movement and window invariants |
| 3 | `stacks/`, `linked_lists/` | LIFO state, sentinel nodes, pointer ownership |
| 4 | `binary_search/`, `heaps_intervals/` | Search spaces, ordering, priority, interval merging |
| 5 | `trees_graphs/` | DFS/BFS, visited state, topological ordering |
| 6 | `backtracking/` | Choice, constraint, undo, duplicate avoidance |
| 7 | `dynamic_programming/` | State, transition, base case, iteration order |

## Running and reviewing

Run the complete course:

```bash
./run-tests.sh
```

Then compile one class:

```bash
mkdir -p out
javac --release 17 -d out arrays_strings/rotate_array/RotateArray.java
```

Read the class Javadoc before the method. It documents the contract, invariant,
and complexity. Then inspect `tests/CourseTests.java` to see which behavior is
proved by executable checks.

## Interview answer template

```text
The straightforward approach is ...
It costs ...
The better observation is ...
My invariant is ...
At each iteration ...
The complexity is ...
I would test empty input, duplicates, extreme values, and ...
```

Never say “this is O(1) space” without checking whether the returned output,
recursion stack, or copied collection is excluded from that statement.
