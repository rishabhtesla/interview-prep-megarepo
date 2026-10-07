import java.nio.file.*;
import java.util.*;

/**
 * Deterministic regression suite. Assertions are explicit and work without -ea.
 * Each named solution gets typical and boundary cases; mutation/aliasing, invalid
 * contracts, overflow, repeated invocation, and seeded oracle checks supplement them.
 */
public final class CourseTests {
    private static int assertions;
    private static final Set<String> covered = new TreeSet<>();

    public static void main(String[] args) throws Exception {
        arraysAndWindows();
        stacksAndSearch();
        listsAndTrees();
        graphs();
        heapsAndBacktracking();
        dynamicProgramming();
        oracleChecks();
        eq(50, covered.size());
        if (args.length != 1) throw new IllegalArgumentException("pass solution-source manifest");
        Set<String> sourceNames = new TreeSet<>();
        for (String path : Files.readAllLines(Path.of(args[0]))) {
            String name = Path.of(path).getFileName().toString().replace(".java", "");
            check(sourceNames.add(name), "unique default-package class name");
            Class.forName(name);
            String source = Files.readString(Path.of(path));
            check(source.contains("https://leetcode.com/problems/"), "official link: " + name);
            check(source.contains("public class " + name), "public solution: " + name);
        }
        eq(sourceNames, covered);
        String readme = Files.readString(Path.of("README.md"));
        for (String path : Files.readAllLines(Path.of(args[0]))) {
            String relative = path.startsWith("./") ? path.substring(2) : path;
            check(readme.contains("](" + relative + ")"), "catalog link: " + relative);
        }
        System.out.println("PASS: exactly " + covered.size() + " solutions; "
                + assertions + " explicit assertions, including seeded oracle checks.");
    }

    private static void arraysAndWindows() {
        test("TwoSum", () -> {
            TwoSum s = new TwoSum();
            int[] input = {2, 7, 11, 15};
            arr(s.solve(input, 9), 0, 1);
            arr(input, 2, 7, 11, 15);
            arr(s.solve(new int[]{3, 3}, 6), 0, 1);
            arr(s.solve(new int[0], 0));
            arr(s.solve(new int[]{1}, 2));
            arr(s.solve(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE}, -2));
            throwsType(NullPointerException.class, () -> s.solve(null, 0));
        });
        test("ValidAnagram", () -> {
            ValidAnagram s = new ValidAnagram();
            eq(true, s.solve("anagram", "nagaram"));
            eq(false, s.solve("rat", "car"));
            eq(true, s.solve("", ""));
            eq(false, s.solve("a", ""));
            throwsType(IllegalArgumentException.class, () -> s.solve("A", ""));
            throwsType(NullPointerException.class, () -> s.solve(null, ""));
        });
        test("ContainsDuplicate", () -> {
            ContainsDuplicate s = new ContainsDuplicate();
            eq(true, s.solve(new int[]{1, 2, 3, 1}));
            eq(false, s.solve(new int[]{1, 2, 3}));
            eq(false, s.solve(new int[0]));
            eq(true, s.solve(new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE}));
            throwsType(NullPointerException.class, () -> s.solve(null));
        });
        test("GroupAnagrams", () -> {
            GroupAnagrams s = new GroupAnagrams();
            String[] input = {"eat", "tea", "tan", "ate", "nat", "bat"};
            eq(List.of(List.of("eat", "tea", "ate"), List.of("tan", "nat"), List.of("bat")), s.solve(input));
            eq("eat", input[0]);
            eq(List.of(), s.solve(new String[0]));
            eq(List.of(List.of("", ""), List.of("A")), s.solve(new String[]{"", "A", ""}));
            throwsType(NullPointerException.class, () -> s.solve(new String[]{null}));
        });
        test("ProductExceptSelf", () -> {
            ProductExceptSelf s = new ProductExceptSelf();
            int[] input = {1, 2, 3, 4};
            longs(s.solve(input), 24, 12, 8, 6);
            arr(input, 1, 2, 3, 4);
            longs(s.solve(new int[]{0, 1, 2}), 2, 0, 0);
            longs(s.solve(new int[]{0, 1, 0}), 0, 0, 0);
            longs(s.solve(new int[]{-1, 2, -3}), -6, 3, -2);
            longs(s.solve(new int[]{7}), 1);
            longs(s.solve(new int[0]));
            throwsType(ArithmeticException.class, () -> s.solve(new int[]{
                    Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE}));
        });
        test("LongestConsecutive", () -> {
            LongestConsecutive s = new LongestConsecutive();
            eq(4, s.solve(new int[]{100, 4, 200, 1, 3, 2, 2}));
            eq(0, s.solve(new int[0]));
            eq(2, s.solve(new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MAX_VALUE}));
            eq(3, s.solve(new int[]{-1, 0, 1}));
        });
        test("ValidPalindrome", () -> {
            ValidPalindrome s = new ValidPalindrome();
            eq(true, s.solve("A man, a plan, a canal: Panama"));
            eq(false, s.solve("race a car"));
            eq(true, s.solve(""));
            eq(true, s.solve("..."));
            eq(false, s.solve("0P"));
            eq(true, s.solve("\u00e9A")); // Non-ASCII is ignored by this explicit contract.
        });
        test("TwoSumSorted", () -> {
            TwoSumSorted s = new TwoSumSorted();
            arr(s.solve(new int[]{2, 7, 11, 15}, 9), 1, 2);
            arr(s.solve(new int[]{3, 3}, 6), 1, 2);
            arr(s.solve(new int[0], 0));
            arr(s.solve(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE}, -2));
        });
        test("ThreeSum", () -> {
            ThreeSum s = new ThreeSum();
            int[] input = {-1, 0, 1, 2, -1, -4};
            eq(List.of(List.of(-1, -1, 2), List.of(-1, 0, 1)), s.solve(input));
            arr(input, -1, 0, 1, 2, -1, -4);
            eq(List.of(List.of(0, 0, 0)), s.solve(new int[]{0, 0, 0, 0}));
            eq(List.of(), s.solve(new int[0]));
            eq(List.of(), s.solve(new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, 0}));
        });
        test("ContainerMostWater", () -> {
            ContainerMostWater s = new ContainerMostWater();
            eq(49, s.solve(new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7}));
            eq(0, s.solve(new int[0]));
            eq(0, s.solve(new int[]{9}));
            eq(4294967294L, s.solve(new int[]{Integer.MAX_VALUE, 0, Integer.MAX_VALUE}));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{-1}));
        });
        test("LongestSubstring", () -> {
            LongestSubstring s = new LongestSubstring();
            eq(3, s.solve("abcabcbb"));
            eq(2, s.solve("abba"));
            eq(0, s.solve(""));
            eq(1, s.solve("bbbbb"));
            eq(3, s.solve("\u00e9\u03bb\u4e2d\u00e9"));
        });
        test("CharacterReplacement", () -> {
            CharacterReplacement s = new CharacterReplacement();
            eq(4, s.solve("AABABBA", 1));
            eq(0, s.solve("", 0));
            eq(2, s.solve("AAB", 0));
            eq(3, s.solve("ABC", 100));
            throwsType(IllegalArgumentException.class, () -> s.solve("a", 0));
            throwsType(IllegalArgumentException.class, () -> s.solve("", -1));
        });
        test("MinimumWindow", () -> {
            MinimumWindow s = new MinimumWindow();
            eq("BANC", s.solve("ADOBECODEBANC", "ABC"));
            eq("AAB", s.solve("AAAB", "AAB"));
            eq("", s.solve("a", "aa"));
            eq("", s.solve("", "a"));
            eq("", s.solve("abc", ""));
            eq("ab", s.solve("abxxba", "ab"));
            eq("\u03bb\u00e9", s.solve("x\u03bb\u00e9", "\u00e9\u03bb"));
        });
    }

    private static void stacksAndSearch() {
        test("ValidParentheses", () -> {
            ValidParentheses s = new ValidParentheses();
            eq(true, s.solve("([]){}"));
            eq(false, s.solve("([)]"));
            eq(false, s.solve("("));
            eq(false, s.solve("]"));
            eq(true, s.solve(""));
            throwsType(IllegalArgumentException.class, () -> s.solve("]x"));
        });
        test("DailyTemperatures", () -> {
            DailyTemperatures s = new DailyTemperatures();
            arr(s.solve(new int[]{73, 74, 75, 71, 69, 72, 76, 73}), 1, 1, 4, 2, 1, 1, 0, 0);
            arr(s.solve(new int[]{30, 30, 31}), 2, 1, 0);
            arr(s.solve(new int[]{3, 2, 1}), 0, 0, 0);
            arr(s.solve(new int[0]));
        });
        test("LargestRectangle", () -> {
            LargestRectangle s = new LargestRectangle();
            eq(10, s.solve(new int[]{2, 1, 5, 6, 2, 3}));
            eq(4, s.solve(new int[]{2, 2}));
            eq(0, s.solve(new int[0]));
            eq(0, s.solve(new int[]{0, 0}));
            eq(4294967294L, s.solve(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE}));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{-2}));
        });
        test("BinarySearch", () -> {
            BinarySearch s = new BinarySearch();
            eq(4, s.solve(new int[]{-1, 0, 3, 5, 9, 12}, 9));
            eq(0, s.solve(new int[]{1, 1, 1}, 1));
            eq(-1, s.solve(new int[0], 3));
            eq(-1, s.solve(new int[]{1}, 2));
            eq(1, s.solve(new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE}, Integer.MAX_VALUE));
        });
        test("SearchRotated", () -> {
            SearchRotated s = new SearchRotated();
            eq(4, s.solve(new int[]{4, 5, 6, 7, 0, 1, 2}, 0));
            eq(-1, s.solve(new int[]{4, 5, 6, 7, 0, 1, 2}, 3));
            eq(-1, s.solve(new int[0], 0));
            eq(0, s.solve(new int[]{1}, 1));
            eq(1, s.solve(new int[]{3, 1}, 1));
        });
        test("FindRotatedMinimum", () -> {
            FindRotatedMinimum s = new FindRotatedMinimum();
            eq(1, s.solve(new int[]{3, 4, 5, 1, 2}));
            eq(1, s.solve(new int[]{1, 2, 3}));
            eq(7, s.solve(new int[]{7}));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[0]));
        });
        test("KokoBananas", () -> {
            KokoBananas s = new KokoBananas();
            eq(4, s.solve(new int[]{3, 6, 7, 11}, 8));
            eq(30, s.solve(new int[]{30, 11, 23, 4, 20}, 5));
            eq(-1, s.solve(new int[]{1, 1}, 1));
            eq(0, s.solve(new int[0], 0));
            eq(1, s.solve(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE}, 4294967294L));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{0}, 1));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[0], -1));
        });
    }

    private static void listsAndTrees() {
        test("ReverseList", () -> {
            ReverseList s = new ReverseList();
            ListNode head = list(1, 2, 3), oldTail = head.next.next;
            ListNode reversed = s.solve(head);
            check(reversed == oldTail, "reverse must reuse tail");
            arr(values(reversed), 3, 2, 1);
            check(head.next == null, "original head becomes tail");
            check(s.solve(reversed) == head, "double reverse restores head");
            arr(values(head), 1, 2, 3);
            eq(null, s.solve(null));
            ListNode single = list(1);
            check(s.solve(single) == single, "singleton identity");
        });
        test("MergeTwoLists", () -> {
            MergeTwoLists s = new MergeTwoLists();
            ListNode first = list(1, 2, 4), second = list(1, 3, 4);
            ListNode merged = s.solve(first, second);
            arr(values(merged), 1, 1, 2, 3, 4, 4);
            check(merged == first && merged.next == second, "stable reused nodes");
            eq(null, s.solve(null, null));
            ListNode single = list(8);
            check(s.solve(null, single) == single, "empty side aliases other head");
        });
        test("LinkedListCycle", () -> {
            LinkedListCycle s = new LinkedListCycle();
            ListNode head = list(3, 2, 0, -4), entry = head.next;
            head.next.next.next.next = entry;
            eq(true, s.solve(head));
            check(head.next.next.next.next == entry, "cycle remains unchanged");
            eq(false, s.solve(list(1, 1)));
            eq(false, s.solve(null));
            ListNode self = list(1);
            self.next = self;
            eq(true, s.solve(self));
        });
        test("RemoveNthFromEnd", () -> {
            RemoveNthFromEnd s = new RemoveNthFromEnd();
            ListNode head = list(1, 2, 3, 4, 5);
            check(s.solve(head, 2) == head, "head retained");
            arr(values(head), 1, 2, 3, 5);
            eq(null, s.solve(list(1), 1));
            ListNode pair = list(1, 2), second = pair.next;
            check(s.solve(pair, 2) == second, "head removal");
            ListNode safe = list(1, 2);
            throwsType(IllegalArgumentException.class, () -> s.solve(safe, 3));
            arr(values(safe), 1, 2);
            throwsType(IllegalArgumentException.class, () -> s.solve(safe, 0));
            throwsType(IllegalArgumentException.class, () -> s.solve(null, 1));
        });
        test("MaximumDepth", () -> {
            MaximumDepth s = new MaximumDepth();
            eq(3, s.solve(sampleTree()));
            eq(0, s.solve(null));
            eq(1, s.solve(new TreeNode(1)));
            eq(3, s.solve(new TreeNode(1, new TreeNode(2, new TreeNode(3), null), null)));
        });
        test("InvertTree", () -> {
            InvertTree s = new InvertTree();
            TreeNode tree = sampleTree(), left = tree.left, right = tree.right;
            String before = treeString(tree);
            check(s.solve(tree) == tree, "root reused");
            check(tree.left == right && tree.right == left, "children swapped");
            eq(List.of(List.of(3), List.of(20, 9), List.of(7, 15)), new LevelOrder().solve(tree));
            s.solve(tree);
            eq(before, treeString(tree));
            eq(null, s.solve(null));
        });
        test("LevelOrder", () -> {
            LevelOrder s = new LevelOrder();
            TreeNode tree = sampleTree();
            String before = treeString(tree);
            eq(List.of(List.of(3), List.of(9, 20), List.of(15, 7)), s.solve(tree));
            eq(before, treeString(tree));
            eq(List.of(), s.solve(null));
            eq(List.of(List.of(1)), s.solve(new TreeNode(1)));
        });
        test("ValidateBST", () -> {
            ValidateBST s = new ValidateBST();
            eq(true, s.solve(bst()));
            eq(false, s.solve(new TreeNode(5, new TreeNode(1),
                    new TreeNode(7, new TreeNode(4), new TreeNode(8)))));
            eq(false, s.solve(new TreeNode(1, new TreeNode(1), null)));
            eq(true, s.solve(null));
            eq(true, s.solve(new TreeNode(0, new TreeNode(Integer.MIN_VALUE), new TreeNode(Integer.MAX_VALUE))));
        });
        test("LowestCommonAncestorBST", () -> {
            LowestCommonAncestorBST s = new LowestCommonAncestorBST();
            TreeNode tree = bst();
            check(s.solve(tree, 2, 8) == tree, "split root");
            check(s.solve(tree, 2, 4) == tree.left, "ancestor is target");
            check(s.solve(tree, 4, 4) == tree.left.right, "same target");
            eq(null, s.solve(tree, 2, 99));
            eq(null, s.solve(null, 1, 2));
        });
        test("Diameter", () -> {
            Diameter s = new Diameter();
            eq(3, s.solve(new TreeNode(1, new TreeNode(2, new TreeNode(4), new TreeNode(5)), new TreeNode(3))));
            eq(0, s.solve(null)); // Same instance: no stale aggregate.
            eq(0, s.solve(new TreeNode(1)));
            TreeNode lower = new TreeNode(0,
                    new TreeNode(1, new TreeNode(2), null),
                    new TreeNode(3, null, new TreeNode(4)));
            eq(4, s.solve(new TreeNode(9, lower, null))); // Best need not pass through root.
        });
    }

    private static void graphs() {
        test("NumberOfIslands", () -> {
            NumberOfIslands s = new NumberOfIslands();
            char[][] grid = {"11000".toCharArray(), "11000".toCharArray(), "00100".toCharArray(), "00011".toCharArray()};
            eq(3, s.solve(grid));
            for (char[] row : grid) for (char c : row) eq('0', c);
            eq(0, s.solve(grid));
            eq(0, s.solve(new char[0][]));
            eq(0, s.solve(new char[][]{{}, {}}));
            eq(2, s.solve(new char[][]{{'1', '0'}, {'0', '1'}}));
            char[][] invalid = {{'1'}, {'x'}};
            throwsType(IllegalArgumentException.class, () -> s.solve(invalid));
            eq('1', invalid[0][0]);
            throwsType(IllegalArgumentException.class, () -> s.solve(new char[][]{{'1'}, {}}));
        });
        test("CloneGraph", () -> {
            CloneGraph s = new CloneGraph();
            GraphNode first = new GraphNode(7), second = new GraphNode(7);
            first.neighbors.add(second);
            first.neighbors.add(first);
            first.neighbors.add(second);
            second.neighbors.add(first);
            GraphNode copy = s.solve(first), copySecond = copy.neighbors.get(0);
            check(copy != first && copySecond != second && copy != copySecond, "deep identity copy");
            eq(7, copy.value);
            eq(7, copySecond.value);
            eq(3, copy.neighbors.size());
            check(copy.neighbors.get(1) == copy, "self loop preserved");
            check(copy.neighbors.get(2) == copySecond, "parallel edge sharing");
            check(copySecond.neighbors.get(0) == copy, "back edge preserved");
            check(first.neighbors.get(0) == second, "source unchanged");
            eq(null, s.solve(null));
            GraphNode isolated = s.solve(new GraphNode(2));
            eq(2, isolated.value);
            eq(0, isolated.neighbors.size());
        });
        test("CourseSchedule", () -> {
            CourseSchedule s = new CourseSchedule();
            eq(true, s.solve(4, new int[][]{{1, 0}, {2, 0}, {3, 1}, {3, 2}}));
            eq(false, s.solve(2, new int[][]{{1, 0}, {0, 1}}));
            eq(false, s.solve(1, new int[][]{{0, 0}}));
            eq(true, s.solve(2, new int[][]{{1, 0}, {1, 0}}));
            eq(true, s.solve(0, new int[0][]));
            eq(true, s.solve(3, new int[0][]));
            throwsType(IllegalArgumentException.class, () -> s.solve(2, new int[][]{{2, 0}}));
            throwsType(IllegalArgumentException.class, () -> s.solve(-1, new int[0][]));
        });
        test("RottingOranges", () -> {
            RottingOranges s = new RottingOranges();
            int[][] grid = {{2, 1, 1}, {1, 1, 0}, {0, 1, 1}};
            eq(4, s.solve(grid));
            matrix(new int[][]{{2, 2, 2}, {2, 2, 0}, {0, 2, 2}}, grid);
            int[][] blocked = {{2, 1, 0, 1}};
            eq(-1, s.solve(blocked));
            matrix(new int[][]{{2, 2, 0, 1}}, blocked);
            eq(0, s.solve(new int[0][]));
            eq(0, s.solve(new int[][]{{0, 2}}));
            eq(1, s.solve(new int[][]{{2, 1, 2}}));
            int[][] invalid = {{2, 1}, {0, 3}};
            throwsType(IllegalArgumentException.class, () -> s.solve(invalid));
            eq(1, invalid[0][1]);
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[][]{{1}, {}}));
        });
        test("RedundantConnection", () -> {
            RedundantConnection s = new RedundantConnection();
            int[][] edges = {{1, 2}, {1, 3}, {2, 3}};
            int[] redundant = s.solve(edges);
            arr(redundant, 2, 3);
            check(redundant != edges[2], "returned edge copied");
            arr(s.solve(new int[][]{{1, 2}, {2, 3}, {3, 4}, {1, 4}, {1, 5}}), 1, 4);
            arr(s.solve(new int[0][]));
            arr(s.solve(new int[][]{{1, 1}}), 1, 1);
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[][]{{1, 2}}));
        });
    }

    private static void heapsAndBacktracking() {
        test("KthLargest", () -> {
            KthLargest s = new KthLargest();
            int[] input = {3, 2, 1, 5, 6, 4};
            eq(5, s.solve(input, 2));
            arr(input, 3, 2, 1, 5, 6, 4);
            eq(4, s.solve(new int[]{3, 2, 3, 1, 2, 4, 5, 5, 6}, 4));
            eq(-1, s.solve(new int[]{-1}, 1));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[0], 1));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{1}, 0));
        });
        test("TopKFrequent", () -> {
            TopKFrequent s = new TopKFrequent();
            arr(s.solve(new int[]{1, 1, 1, 2, 2, 3}, 2), 1, 2);
            arr(s.solve(new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE, 0}, 2), Integer.MIN_VALUE, 0);
            arr(s.solve(new int[]{2, 1, 2, 1}, 2), 1, 2);
            arr(s.solve(new int[0], 0));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{1}, 2));
        });
        test("MergeIntervals", () -> {
            MergeIntervals s = new MergeIntervals();
            int[][] input = {{8, 10}, {1, 3}, {2, 6}, {15, 18}};
            int[][] result = s.solve(input);
            matrix(new int[][]{{1, 6}, {8, 10}, {15, 18}}, result);
            matrix(new int[][]{{8, 10}, {1, 3}, {2, 6}, {15, 18}}, input);
            result[1][0] = 99;
            eq(8, input[0][0]);
            matrix(new int[][]{{1, 5}}, s.solve(new int[][]{{1, 4}, {4, 5}, {2, 3}}));
            matrix(new int[0][], s.solve(new int[0][]));
            matrix(new int[][]{{Integer.MIN_VALUE, Integer.MAX_VALUE}},
                    s.solve(new int[][]{{0, Integer.MAX_VALUE}, {Integer.MIN_VALUE, 0}}));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[][]{{3, 1}}));
        });
        test("InsertInterval", () -> {
            InsertInterval s = new InsertInterval();
            int[][] input = {{1, 2}, {3, 5}, {6, 7}, {8, 10}, {12, 16}};
            int[] added = {4, 8};
            int[][] result = s.solve(input, added);
            matrix(new int[][]{{1, 2}, {3, 10}, {12, 16}}, result);
            arr(added, 4, 8);
            matrix(new int[][]{{1, 2}, {3, 5}, {6, 7}, {8, 10}, {12, 16}}, input);
            result[0][0] = 99;
            eq(1, input[0][0]);
            matrix(new int[][]{{2, 3}}, s.solve(new int[0][], new int[]{2, 3}));
            matrix(new int[][]{{1, 5}}, s.solve(new int[][]{{1, 2}, {4, 5}}, new int[]{2, 4}));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[][]{{3, 4}, {1, 2}}, new int[]{5, 6}));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[0][], new int[]{2, 1}));
        });
        test("Subsets", () -> {
            Subsets s = new Subsets();
            int[] input = {1, 2};
            eq(List.of(List.of(), List.of(1), List.of(1, 2), List.of(2)), s.solve(input));
            arr(input, 1, 2);
            eq(List.of(List.of()), s.solve(new int[0]));
            List<List<Integer>> result = s.solve(new int[]{1});
            result.get(0).add(9);
            eq(List.of(1), result.get(1));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{1, 1}));
        });
        test("Permutations", () -> {
            Permutations s = new Permutations();
            int[] input = {1, 2, 3};
            List<List<Integer>> result = s.solve(input);
            eq(Set.of(List.of(1, 2, 3), List.of(1, 3, 2), List.of(2, 1, 3),
                    List.of(2, 3, 1), List.of(3, 1, 2), List.of(3, 2, 1)), new HashSet<>(result));
            eq(6, result.size());
            arr(input, 1, 2, 3);
            eq(List.of(List.of()), s.solve(new int[0]));
            eq(List.of(List.of(9)), s.solve(new int[]{9}));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{1, 1}));
        });
        test("CombinationSum", () -> {
            CombinationSum s = new CombinationSum();
            int[] input = {7, 3, 2, 6};
            eq(List.of(List.of(2, 2, 3), List.of(7)), s.solve(input, 7));
            arr(input, 7, 3, 2, 6);
            eq(List.of(List.of()), s.solve(new int[0], 0));
            eq(List.of(), s.solve(new int[]{2}, 1));
            eq(List.of(), s.solve(new int[0], 1));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{0, 1}, 1));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{1, 1}, 2));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{1}, -1));
        });
        test("WordSearch", () -> {
            WordSearch s = new WordSearch();
            char[][] board = {"ABCE".toCharArray(), "SFCS".toCharArray(), "ADEE".toCharArray()};
            String before = Arrays.deepToString(board);
            eq(true, s.solve(board, "ABCCED"));
            eq(before, Arrays.deepToString(board));
            eq(true, s.solve(board, "SEE"));
            eq(false, s.solve(board, "ABCB"));
            eq(before, Arrays.deepToString(board));
            eq(true, s.solve(new char[0][], ""));
            eq(false, s.solve(new char[0][], "A"));
            eq(false, s.solve(new char[][]{{'A', 'B', 'X'}}, "ABA"));
            eq(true, s.solve(new char[][]{{'\0'}}, "\0"));
            throwsType(IllegalArgumentException.class, () -> s.solve(new char[][]{{'A'}, {}}, ""));
        });
    }

    private static void dynamicProgramming() {
        test("ClimbingStairs", () -> {
            ClimbingStairs s = new ClimbingStairs();
            eq(8, s.solve(5));
            eq(1, s.solve(0));
            eq(1, s.solve(1));
            eq(2, s.solve(2));
            eq(7540113804746346429L, s.solve(91));
            throwsType(IllegalArgumentException.class, () -> s.solve(-1));
            throwsType(IllegalArgumentException.class, () -> s.solve(92));
        });
        test("HouseRobber", () -> {
            HouseRobber s = new HouseRobber();
            eq(12, s.solve(new int[]{2, 7, 9, 3, 1}));
            eq(4, s.solve(new int[]{2, 3, 2}));
            eq(0, s.solve(new int[0]));
            eq(7, s.solve(new int[]{7}));
            eq(4294967294L, s.solve(new int[]{Integer.MAX_VALUE, 0, Integer.MAX_VALUE}));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{-1}));
        });
        test("CoinChange", () -> {
            CoinChange s = new CoinChange();
            eq(3, s.solve(new int[]{1, 2, 5}, 11));
            eq(2, s.solve(new int[]{1, 3, 4}, 6));
            eq(-1, s.solve(new int[]{2}, 3));
            eq(0, s.solve(new int[0], 0));
            eq(-1, s.solve(new int[0], 1));
            eq(2, s.solve(new int[]{1, 1, 2}, 4));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{0}, 0));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{1}, -1));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[]{1}, Integer.MAX_VALUE));
        });
        test("LongestIncreasingSubsequence", () -> {
            LongestIncreasingSubsequence s = new LongestIncreasingSubsequence();
            eq(4, s.solve(new int[]{10, 9, 2, 5, 3, 7, 101, 18}));
            eq(1, s.solve(new int[]{7, 7, 7}));
            eq(0, s.solve(new int[0]));
            eq(1, s.solve(new int[]{5, 4, 3, 2}));
            eq(2, s.solve(new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE}));
        });
        test("LongestCommonSubsequence", () -> {
            LongestCommonSubsequence s = new LongestCommonSubsequence();
            eq(3, s.solve("abcde", "ace"));
            eq(0, s.solve("abc", "def"));
            eq(0, s.solve("", "abc"));
            eq(2, s.solve("aaaa", "aa"));
            eq(2, s.solve("ace", "abc"));
            eq(3, s.solve("abc", "abc"));
        });
        test("MaximumSubarray", () -> {
            MaximumSubarray s = new MaximumSubarray();
            eq(6, s.solve(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4}));
            eq(-1, s.solve(new int[]{-3, -1, -2}));
            eq(-5, s.solve(new int[]{-5}));
            eq(4294967294L, s.solve(new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE}));
            throwsType(IllegalArgumentException.class, () -> s.solve(new int[0]));
        });
        test("WordBreak", () -> {
            WordBreak s = new WordBreak();
            eq(true, s.solve("leetcode", List.of("leet", "code")));
            eq(true, s.solve("applepenapple", List.of("apple", "pen")));
            eq(false, s.solve("catsandog", List.of("cats", "dog", "sand", "and", "cat")));
            eq(true, s.solve("", List.of()));
            eq(false, s.solve("a", List.of()));
            eq(true, s.solve("aaaa", List.of("a", "a")));
            throwsType(IllegalArgumentException.class, () -> s.solve("", List.of("")));
            throwsType(NullPointerException.class, () -> s.solve(null, List.of()));
        });
    }

    private static void oracleChecks() {
        Random random = new Random(20260930L);
        for (int trial = 0; trial < 160; trial++) {
            int n = random.nextInt(9);
            int[] values = new int[n], nonnegative = new int[n];
            for (int i = 0; i < n; i++) {
                values[i] = random.nextInt(11) - 5;
                nonnegative[i] = random.nextInt(8);
            }
            int[] snapshot = values.clone();
            eq(bruteLis(values), new LongestIncreasingSubsequence().solve(values));
            eq(bruteRobber(nonnegative), new HouseRobber().solve(nonnegative));
            eq(bruteRectangle(nonnegative), new LargestRectangle().solve(nonnegative));
            eq(bruteWater(nonnegative), new ContainerMostWater().solve(nonnegative));
            arr(new DailyTemperatures().solve(values), bruteTemperatures(values));
            if (n > 0) {
                eq(bruteMaximumSubarray(values), new MaximumSubarray().solve(values));
                int[] sorted = values.clone();
                Arrays.sort(sorted);
                for (int k = 1; k <= n; k++) eq(sorted[n - k], new KthLargest().solve(values, k));
            }
            long[] products = new long[n];
            for (int i = 0; i < n; i++) {
                products[i] = 1;
                for (int j = 0; j < n; j++) if (j != i) products[i] *= values[j];
            }
            longs(new ProductExceptSelf().solve(values), products);
            eq(bruteThreeSum(values), new TreeSet<>(stringTriples(new ThreeSum().solve(values))));
            arr(values, snapshot);
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < n; i++) text.append((char) ('A' + random.nextInt(3)));
            int budget = random.nextInt(4);
            eq(bruteReplacement(text.toString(), budget), new CharacterReplacement().solve(text.toString(), budget));
            eq(bruteMinimumWindow(text.toString(), "AAB"), new MinimumWindow().solve(text.toString(), "AAB"));
        }
        for (int n = 1; n <= 12; n++) {
            for (int rotation = 0; rotation < n; rotation++) {
                int[] rotated = new int[n];
                for (int i = 0; i < n; i++) rotated[i] = (i + rotation) % n;
                eq(0, new FindRotatedMinimum().solve(rotated));
                for (int i = 0; i < n; i++) eq(i, new SearchRotated().solve(rotated, rotated[i]));
                eq(-1, new SearchRotated().solve(rotated, -1));
            }
        }
    }

    private static int bruteLis(int[] nums) {
        int best = 0;
        for (int mask = 0; mask < (1 << nums.length); mask++) {
            long previous = Long.MIN_VALUE;
            int length = 0;
            boolean valid = true;
            for (int i = 0; i < nums.length; i++) if ((mask & (1 << i)) != 0) {
                if (nums[i] <= previous) valid = false;
                previous = nums[i];
                length++;
            }
            if (valid) best = Math.max(best, length);
        }
        return best;
    }

    private static long bruteRobber(int[] values) {
        long best = 0;
        for (int mask = 0; mask < (1 << values.length); mask++) {
            if ((mask & (mask << 1)) != 0) continue;
            long sum = 0;
            for (int i = 0; i < values.length; i++) if ((mask & (1 << i)) != 0) sum += values[i];
            best = Math.max(best, sum);
        }
        return best;
    }

    private static long bruteRectangle(int[] heights) {
        long best = 0;
        for (int left = 0; left < heights.length; left++) {
            int min = Integer.MAX_VALUE;
            for (int right = left; right < heights.length; right++) {
                min = Math.min(min, heights[right]);
                best = Math.max(best, (long) min * (right - left + 1));
            }
        }
        return best;
    }

    private static long bruteWater(int[] heights) {
        long best = 0;
        for (int i = 0; i < heights.length; i++)
            for (int j = i + 1; j < heights.length; j++)
                best = Math.max(best, (long) (j - i) * Math.min(heights[i], heights[j]));
        return best;
    }

    private static int[] bruteTemperatures(int[] temperatures) {
        int[] result = new int[temperatures.length];
        for (int i = 0; i < temperatures.length; i++)
            for (int j = i + 1; j < temperatures.length; j++)
                if (temperatures[j] > temperatures[i]) { result[i] = j - i; break; }
        return result;
    }

    private static long bruteMaximumSubarray(int[] nums) {
        long best = Long.MIN_VALUE;
        for (int i = 0; i < nums.length; i++) {
            long sum = 0;
            for (int j = i; j < nums.length; j++) { sum += nums[j]; best = Math.max(best, sum); }
        }
        return best;
    }

    private static Set<String> bruteThreeSum(int[] nums) {
        Set<String> result = new TreeSet<>();
        for (int i = 0; i < nums.length; i++)
            for (int j = i + 1; j < nums.length; j++)
                for (int k = j + 1; k < nums.length; k++)
                    if ((long) nums[i] + nums[j] + nums[k] == 0) {
                        int[] triple = {nums[i], nums[j], nums[k]};
                        Arrays.sort(triple);
                        result.add(Arrays.toString(triple));
                    }
        return result;
    }

    private static List<String> stringTriples(List<List<Integer>> triples) {
        List<String> result = new ArrayList<>();
        for (List<Integer> triple : triples) result.add(triple.toString());
        check(new HashSet<>(result).size() == result.size(), "no duplicate triples");
        return result;
    }

    private static int bruteReplacement(String text, int k) {
        int best = 0;
        for (int i = 0; i < text.length(); i++) {
            int[] counts = new int[26];
            for (int j = i; j < text.length(); j++) {
                counts[text.charAt(j) - 'A']++;
                int maximum = Arrays.stream(counts).max().orElse(0);
                if (j - i + 1 - maximum <= k) best = Math.max(best, j - i + 1);
            }
        }
        return best;
    }

    private static String bruteMinimumWindow(String text, String target) {
        String best = "";
        for (int start = 0; start < text.length(); start++) {
            for (int end = start + 1; end <= text.length(); end++) {
                String candidate = text.substring(start, end);
                Map<Character, Integer> counts = new HashMap<>();
                for (char c : candidate.toCharArray()) counts.merge(c, 1, Integer::sum);
                boolean valid = true;
                for (char c : target.toCharArray()) if (counts.merge(c, -1, Integer::sum) < 0) valid = false;
                if (valid && (best.isEmpty() || candidate.length() < best.length())) best = candidate;
            }
        }
        return best;
    }

    private static ListNode list(int... values) {
        ListNode dummy = new ListNode(0), tail = dummy;
        for (int value : values) { tail.next = new ListNode(value); tail = tail.next; }
        return dummy.next;
    }

    private static int[] values(ListNode head) {
        List<Integer> values = new ArrayList<>();
        Set<ListNode> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        while (head != null) {
            check(visited.add(head), "unexpected cycle in result");
            values.add(head.value);
            head = head.next;
        }
        return values.stream().mapToInt(Integer::intValue).toArray();
    }

    private static TreeNode sampleTree() {
        return new TreeNode(3, new TreeNode(9), new TreeNode(20, new TreeNode(15), new TreeNode(7)));
    }

    private static TreeNode bst() {
        return new TreeNode(6, new TreeNode(2, new TreeNode(0), new TreeNode(4)), new TreeNode(8));
    }

    private static String treeString(TreeNode node) {
        return node == null ? "#" : node.value + "(" + treeString(node.left) + "," + treeString(node.right) + ")";
    }

    private static void test(String name, Runnable body) {
        check(covered.add(name), "duplicate solution test: " + name);
        int before = assertions;
        try { body.run(); }
        catch (Throwable failure) { throw new AssertionError("Solution failed: " + name, failure); }
        check(assertions - before >= 2, "typical and boundary assertions required: " + name);
    }

    private static void eq(long expected, long actual) {
        check(expected == actual, "expected " + expected + ", got " + actual);
    }

    private static void eq(Object expected, Object actual) {
        check(Objects.equals(expected, actual), "expected " + expected + ", got " + actual);
    }

    private static void arr(int[] actual, int... expected) {
        check(Arrays.equals(expected, actual), "expected " + Arrays.toString(expected) + ", got " + Arrays.toString(actual));
    }

    private static void longs(long[] actual, long... expected) {
        check(Arrays.equals(expected, actual), "expected " + Arrays.toString(expected) + ", got " + Arrays.toString(actual));
    }

    private static void matrix(int[][] expected, int[][] actual) {
        check(Arrays.deepEquals(expected, actual), "expected " + Arrays.deepToString(expected) + ", got " + Arrays.deepToString(actual));
    }

    private static void throwsType(Class<? extends Throwable> expected, Runnable body) {
        try { body.run(); }
        catch (Throwable actual) {
            check(expected.isInstance(actual), "expected " + expected.getName() + ", got " + actual);
            return;
        }
        throw new AssertionError("Expected " + expected.getName() + " but operation succeeded");
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
