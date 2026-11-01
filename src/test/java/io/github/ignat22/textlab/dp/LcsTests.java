package io.github.ignat22.textlab.dp;

import io.github.ignat22.textlab.testkit.Check;
import io.github.ignat22.textlab.testkit.Expect;
import io.github.ignat22.textlab.testkit.RandomStrings;

import java.util.Arrays;

public class LcsTests {

    @Check("identical strings: LCS is the whole string")
    void identicalStrings() {
        LcsResult result = LcsSolver.solve("dynamic", "dynamic");
        Expect.equal(7, result.length(), "length");
        Expect.equal("dynamic", result.witness(), "witness");
    }

    @Check("completely different strings: LCS is 0")
    void completelyDifferent() {
        LcsResult result = LcsSolver.solve("abcdef", "uvwxyz");
        Expect.equal(0, result.length(), "length");
        Expect.equal("", result.witness(), "witness");
    }

    @Check("empty string on either side gives 0")
    void emptyString() {
        Expect.equal(0, LcsSolver.length("", "algorithm"), "empty first");
        Expect.equal(0, LcsSolver.length("algorithm", ""), "empty second");
        Expect.equal(0, LcsSolver.length("", ""), "both empty");
    }

    @Check("CLRS example ABCBDAB / BDCABA has LCS length 4")
    void textbookExample() {
        LcsResult result = LcsSolver.solve("ABCBDAB", "BDCABA");
        Expect.equal(4, result.length(), "length");
        Expect.that(isSubsequence(result.witness(), "ABCBDAB") && isSubsequence(result.witness(), "BDCABA"),
                "witness " + result.witness() + " must be common");
    }

    @Check("full DP table for ABCB / BDCAB matches the hand-computed table")
    void handComputedTable() {
        int[][] expected = {
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 1, 1},
                {0, 1, 1, 1, 1, 2},
                {0, 1, 1, 2, 2, 2},
                {0, 1, 1, 2, 2, 3}
        };
        int[][] actual = LcsSolver.buildTable("ABCB", "BDCAB");
        Expect.that(Arrays.deepEquals(expected, actual), "table was " + Arrays.deepToString(actual));
    }

    @Check("LCS is symmetric and bounded by the shorter string")
    void symmetryAndBounds() {
        RandomStrings strings = new RandomStrings(71);
        for (int trial = 0; trial < 200; trial++) {
            String a = strings.next(30, "xyz");
            String b = strings.next(30, "xyz");
            int ab = LcsSolver.length(a, b);
            Expect.equal(ab, LcsSolver.length(b, a), "symmetry for " + a + " / " + b);
            Expect.that(ab <= Math.min(a.length(), b.length()), "bound for " + a + " / " + b);
        }
    }

    @Check("bottom-up table agrees with a memoised top-down recursion on 300 random pairs")
    void agreesWithTopDownOracle() {
        RandomStrings strings = new RandomStrings(72);
        for (int trial = 0; trial < 300; trial++) {
            String a = strings.next(18, "abcd");
            String b = strings.next(18, "abcd");
            LcsResult result = LcsSolver.solve(a, b);
            Expect.equal(topDown(a, b), result.length(), a + " / " + b);
            Expect.equal(result.length(), result.witness().length(), "witness length");
            Expect.that(isSubsequence(result.witness(), a) && isSubsequence(result.witness(), b),
                    "witness must be common for " + a + " / " + b);
        }
    }

    @Check("null input is rejected")
    void nullRejected() {
        Expect.fails(NullPointerException.class, () -> LcsSolver.length(null, "a"), "null first");
    }

    private static int topDown(String a, String b) {
        int[][] memo = new int[a.length() + 1][b.length() + 1];
        for (int[] row : memo) {
            Arrays.fill(row, -1);
        }
        return topDown(a, b, a.length(), b.length(), memo);
    }

    private static int topDown(String a, String b, int i, int j, int[][] memo) {
        if (i == 0 || j == 0) {
            return 0;
        }
        if (memo[i][j] >= 0) {
            return memo[i][j];
        }
        int value = a.charAt(i - 1) == b.charAt(j - 1)
                ? topDown(a, b, i - 1, j - 1, memo) + 1
                : Math.max(topDown(a, b, i - 1, j, memo), topDown(a, b, i, j - 1, memo));
        memo[i][j] = value;
        return value;
    }

    private static boolean isSubsequence(String small, String big) {
        int k = 0;
        for (int i = 0; i < big.length() && k < small.length(); i++) {
            if (big.charAt(i) == small.charAt(k)) {
                k++;
            }
        }
        return k == small.length();
    }
}
