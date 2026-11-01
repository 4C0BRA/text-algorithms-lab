package io.github.ignat22.textlab.suffix;

import io.github.ignat22.textlab.testkit.Check;
import io.github.ignat22.textlab.testkit.Expect;
import io.github.ignat22.textlab.testkit.RandomStrings;

public class KasaiTests {

    @Check("abracadabra: manually verified SA gives LCP 0 1 4 1 1 0 3 0 0 0 2")
    void abracadabraManual() {
        int[] suffixArray = {10, 7, 0, 3, 5, 8, 1, 4, 6, 9, 2};
        int[] expectedLcp = {0, 1, 4, 1, 1, 0, 3, 0, 0, 0, 2};
        LcpResult result = KasaiLcp.build("abracadabra", suffixArray);
        Expect.arrayEqual(expectedLcp, result.lcp(), "lcp");
        Expect.equal(4, result.max(), "longest repeated substring length (abra)");
        Expect.equal(54L, result.distinctSubstrings(), "distinct substrings = 66 - 12");
    }

    @Check("aaaa: SA 3 2 1 0, LCP 0 1 2 3")
    void allSameCharacters() {
        LcpResult result = KasaiLcp.build("aaaa", new int[]{3, 2, 1, 0});
        Expect.arrayEqual(new int[]{0, 1, 2, 3}, result.lcp(), "lcp");
    }

    @Check("empty string and single character")
    void tinyInputs() {
        Expect.arrayEqual(new int[0], KasaiLcp.build("", new int[0]).lcp(), "empty");
        Expect.arrayEqual(new int[]{0}, KasaiLcp.build("q", new int[]{0}).lcp(), "single");
    }

    @Check("Kasai agrees with brute-force LCP of neighbours on 400 random strings")
    void agreesWithBruteForce() {
        RandomStrings strings = new RandomStrings(121);
        for (int trial = 0; trial < 400; trial++) {
            String s = strings.next(120, trial % 2 == 0 ? "ab" : "abcdefgh");
            int[] sa = SuffixArrays.byPrefixDoubling(s);
            Expect.arrayEqual(bruteForceLcp(s, sa), KasaiLcp.build(s, sa).lcp(), s);
        }
    }

    @Check("character comparisons never exceed 3n")
    void linearComparisonBound() {
        RandomStrings strings = new RandomStrings(122);
        for (int trial = 0; trial < 200; trial++) {
            String s = strings.next(500, trial % 2 == 0 ? "a" : "ab");
            LcpResult result = KasaiLcp.build(s, SuffixArrays.byPrefixDoubling(s));
            Expect.that(result.comparisons() <= 3L * s.length(), "comparisons " + result.comparisons() + " for n=" + s.length());
        }
    }

    @Check("invalid suffix arrays are rejected")
    void invalidInput() {
        Expect.fails(IllegalArgumentException.class, () -> KasaiLcp.build("abc", new int[]{0, 1}), "wrong length");
        Expect.fails(IllegalArgumentException.class, () -> KasaiLcp.build("abc", new int[]{0, 0, 1}), "duplicate");
        Expect.fails(IllegalArgumentException.class, () -> KasaiLcp.build("abc", new int[]{0, 1, 3}), "out of range");
    }

    private static int[] bruteForceLcp(String s, int[] sa) {
        int[] lcp = new int[sa.length];
        for (int i = 1; i < sa.length; i++) {
            int k = 0;
            while (sa[i] + k < s.length() && sa[i - 1] + k < s.length()
                    && s.charAt(sa[i] + k) == s.charAt(sa[i - 1] + k)) {
                k++;
            }
            lcp[i] = k;
        }
        return lcp;
    }
}
