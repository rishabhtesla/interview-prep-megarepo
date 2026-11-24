# DSA and LeetCode Patterns

Start with the [step-by-step learning guide](LEARNING_GUIDE.md). Do not only
collect solution classes: state the invariant, run the test, change an edge
case, and explain the trade-off before moving to the next pattern.

Every solution is a standalone Java 17, default-package class with a `solve`
method (the stateful `RandomizedSet` follows the LeetCode API). Each source
contains the official problem link, contract, invariant/logic, and complexity.

## Basic → advanced catalog

### Arrays and strings

| Level | Problem | Solution |
|---|---|---|
| Basic | [1 Two Sum](arrays_strings/two_sum/TwoSum.java), [14 Longest Common Prefix](arrays_strings/longest_common_prefix/LongestCommonPrefix.java), [26 Remove Duplicates](arrays_strings/remove_duplicates/RemoveDuplicates.java), [27 Remove Element](arrays_strings/remove_element/RemoveElement.java), [28 Find Index](arrays_strings/find_index_first_occurrence/FindIndexFirstOccurrence.java), [58 Last Word](arrays_strings/length_of_last_word/LengthOfLastWord.java), [88 Merge Sorted Array](arrays_strings/merge_sorted_array/MergeSortedArray.java), [121 Best Stock](arrays_strings/best_time_to_buy_sell_stock/BestTimeToBuyAndSellStock.java), [122 Best Stock II](arrays_strings/best_time_to_buy_sell_stock_ii/BestTimeToBuyAndSellStockII.java), [169 Majority](arrays_strings/majority_element/MajorityElement.java), [189 Rotate Array](arrays_strings/rotate_array/RotateArray.java), [238 Product Except Self](arrays_strings/product_except_self/ProductExceptSelf.java), [274 H-Index](arrays_strings/h_index/HIndex.java) | Hash maps, prefix/suffix, two pointers, greedy |
| Intermediate | [6 Zigzag](arrays_strings/zigzag_conversion/ZigzagConversion.java), [12 Integer to Roman](arrays_strings/integer_to_roman/IntegerToRoman.java), [13 Roman to Integer](arrays_strings/roman_to_integer/RomanToInteger.java), [42 Trapping Rain Water](arrays_strings/trapping_rain_water/TrappingRainWater.java), [45 Jump Game II](arrays_strings/jump_game_ii/JumpGameII.java), [55 Jump Game](arrays_strings/jump_game/JumpGame.java), [68 Text Justification](arrays_strings/text_justification/TextJustification.java), [80 Remove Duplicates II](arrays_strings/remove_duplicates_ii/RemoveDuplicatesII.java), [134 Gas Station](arrays_strings/gas_station/GasStation.java), [135 Candy](arrays_strings/candy/Candy.java), [151 Reverse Words](arrays_strings/reverse_words/ReverseWords.java), [380 Randomized Set](arrays_strings/randomized_set/RandomizedSet.java) | Greedy, KMP, simulation, in-place compaction |
| Existing patterns | [Valid Anagram](arrays_strings/valid_anagram/ValidAnagram.java), [Contains Duplicate](arrays_strings/contains_duplicate/ContainsDuplicate.java), [Group Anagrams](arrays_strings/group_anagrams/GroupAnagrams.java), [Longest Consecutive](arrays_strings/longest_consecutive/LongestConsecutive.java), [Three Sum](two_pointers/three_sum/ThreeSum.java), [Container](two_pointers/container_most_water/ContainerMostWater.java), [Valid Palindrome](two_pointers/valid_palindrome/ValidPalindrome.java), [Sorted Two Sum](two_pointers/two_sum_sorted/TwoSumSorted.java), [Longest Substring](sliding_window/longest_substring/LongestSubstring.java), [Character Replacement](sliding_window/character_replacement/CharacterReplacement.java), [Minimum Window](sliding_window/minimum_window/MinimumWindow.java) | Frequency maps, windows, sorting, two pointers |

### The existing course

[Backtracking](backtracking/combination_sum/CombinationSum.java) ·
[Binary search](binary_search/binary_search/BinarySearch.java) ·
[Dynamic programming](dynamic_programming/maximum_subarray/MaximumSubarray.java) ·
[Heaps and intervals](heaps_intervals/merge_intervals/MergeIntervals.java) ·
[Linked lists](linked_lists/reverse_list/ReverseList.java) ·
[Stacks](stacks/largest_rectangle/LargestRectangle.java) ·
[Trees and graphs](trees_graphs/number_of_islands/NumberOfIslands.java)

The remaining course solutions are cataloged in their directories:
[backtracking](backtracking/), [binary_search](binary_search/),
[dynamic_programming](dynamic_programming/), [heaps_intervals](heaps_intervals/),
[linked_lists](linked_lists/), [sliding_window](sliding_window/),
[stacks](stacks/), [trees_graphs](trees_graphs/), and [two_pointers](two_pointers/).

Complete source index: [Permutations](backtracking/permutations/Permutations.java),
[Subsets](backtracking/subsets/Subsets.java), [Word Search](backtracking/word_search/WordSearch.java),
[Find Rotated Minimum](binary_search/find_rotated_minimum/FindRotatedMinimum.java),
[Koko Bananas](binary_search/koko_bananas/KokoBananas.java), [Search Rotated](binary_search/search_rotated/SearchRotated.java),
[Climbing Stairs](dynamic_programming/climbing_stairs/ClimbingStairs.java), [Coin Change](dynamic_programming/coin_change/CoinChange.java),
[House Robber](dynamic_programming/house_robber/HouseRobber.java), [LCS](dynamic_programming/longest_common_subsequence/LongestCommonSubsequence.java),
[LIS](dynamic_programming/longest_increasing_subsequence/LongestIncreasingSubsequence.java), [Word Break](dynamic_programming/word_break/WordBreak.java),
[Insert Interval](heaps_intervals/insert_interval/InsertInterval.java), [Kth Largest](heaps_intervals/kth_largest/KthLargest.java),
[Top K Frequent](heaps_intervals/top_k_frequent/TopKFrequent.java), [Linked List Cycle](linked_lists/linked_list_cycle/LinkedListCycle.java),
[Merge Two Lists](linked_lists/merge_two_lists/MergeTwoLists.java), [Remove Nth](linked_lists/remove_nth_from_end/RemoveNthFromEnd.java),
[Daily Temperatures](stacks/daily_temperatures/DailyTemperatures.java), [Valid Parentheses](stacks/valid_parentheses/ValidParentheses.java),
[Clone Graph](trees_graphs/clone_graph/CloneGraph.java), [Course Schedule](trees_graphs/course_schedule/CourseSchedule.java),
[Diameter](trees_graphs/diameter/Diameter.java), [Invert Tree](trees_graphs/invert_tree/InvertTree.java),
[Level Order](trees_graphs/level_order/LevelOrder.java), [LCA BST](trees_graphs/lowest_common_ancestor_bst/LowestCommonAncestorBST.java),
[Maximum Depth](trees_graphs/maximum_depth/MaximumDepth.java), [Redundant Connection](trees_graphs/redundant_connection/RedundantConnection.java),
[Rotting Oranges](trees_graphs/rotting_oranges/RottingOranges.java), [Validate BST](trees_graphs/validate_bst/ValidateBST.java).

## Compile and run

Compile one solution (the class is in the default package):

```bash
mkdir -p out
javac --release 17 -d out arrays_strings/product_except_self/ProductExceptSelf.java
jshell --class-path out
```

Run the complete deterministic regression suite:

```bash
./run-tests.sh
```

Common patterns are chosen to keep the stated bounds honest: prefix/suffix and
in-place pointers are O(1) auxiliary, hash maps and randomized sets are O(n)
space, sorting is O(n log n), and output-producing string problems necessarily
use O(output) space.
