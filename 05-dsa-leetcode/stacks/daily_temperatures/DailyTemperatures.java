import java.util.*;

/**
 * LC 739 (Medium): for each day, report distance to its next strictly warmer day.
 * https://leetcode.com/problems/daily-temperatures/
 *
 * Contract: non-null int temperatures, arbitrary values; absent warmer day -> 0.
 * Empty -> empty; input unchanged.
 * Pattern: monotonic stack of unresolved indices, with nonincreasing temperatures.
 * A new warmer day resolves every colder top: none saw a warmer day earlier or
 * it would already have been popped. Equal temperatures do not resolve each other.
 * Every index is pushed once and popped at most once: O(n) amortized time,
 * O(n) auxiliary plus O(n) output, O(1) call stack. Store indices, not merely values,
 * because the answer is a distance and duplicates are distinct pending days.
 */
public class DailyTemperatures {
    public int[] solve(int[] temperatures) {
        Objects.requireNonNull(temperatures);
        int[] answer = new int[temperatures.length];
        Deque<Integer> pending = new ArrayDeque<>();
        for (int day = 0; day < temperatures.length; day++) {
            while (!pending.isEmpty() && temperatures[day] > temperatures[pending.peek()]) {
                int earlier = pending.pop();
                answer[earlier] = day - earlier;
            }
            pending.push(day);
        }
        return answer;
    }
}
