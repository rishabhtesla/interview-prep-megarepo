import java.util.Objects;

/**
 * LC 875 (Medium): minimum integer rate to finish piles within h hours, one pile
 * per hour at most; unused hourly capacity cannot move to the next pile.
 * https://leetcode.com/problems/koko-eating-bananas/
 *
 * Contract: non-null positive piles, h >= 0; invalid values throw
 * IllegalArgumentException. Empty -> 0; h < pile count -> -1 (impossible).
 * Pattern: binary search on answer. hours(k)=sum(ceil(pile/k)) is nonincreasing.
 * [1,maxPile] therefore has a false-then-true feasibility boundary when h>=n.
 * A feasible mid may be optimal (keep it); an infeasible mid and all lower
 * rates are eliminated. long arithmetic prevents ceiling/sum overflow.
 * O(n log M + n) time for M=maxPile; O(1) auxiliary/output/stack; no mutation.
 */
public class KokoBananas {
    public int solve(int[] piles, long h) {
        Objects.requireNonNull(piles);
        if (h < 0) throw new IllegalArgumentException("negative hours");
        int high = 0;
        for (int pile : piles) {
            if (pile <= 0) throw new IllegalArgumentException("piles must be positive");
            high = Math.max(high, pile);
        }
        if (piles.length == 0) return 0;
        if (h < piles.length) return -1;
        int low = 1;
        while (low < high) {
            int mid = low + (high - low) / 2;
            long hours = 0;
            for (int pile : piles) hours += ((long) pile + mid - 1) / mid;
            if (hours <= h) high = mid;
            else low = mid + 1;
        }
        return low;
    }
}
