package io.github.ignat22.textlab.suffix;

import io.github.ignat22.textlab.testkit.Check;
import io.github.ignat22.textlab.testkit.Expect;
import io.github.ignat22.textlab.testkit.RandomStrings;

public class SuffixArrayTests {

    @Check("abracadabra suffix array matches the hand-sorted list")
    void abracadabra() {
        int[] expected = {10, 7, 0, 3, 5, 8, 1, 4, 6, 9, 2};
        Expect.arrayEqual(expected, SuffixArrays.bySortingSuffixes("abracadabra"), "sorting");
        Expect.arrayEqual(expected, SuffixArrays.byPrefixDoubling("abracadabra"), "prefix doubling");
    }

    @Check("edge cases: empty, one character, all equal characters")
    void edgeCases() {
        Expect.arrayEqual(new int[0], SuffixArrays.byPrefixDoubling(""), "empty");
        Expect.arrayEqual(new int[]{0}, SuffixArrays.byPrefixDoubling("z"), "one char");
        Expect.arrayEqual(new int[]{4, 3, 2, 1, 0}, SuffixArrays.byPrefixDoubling("aaaaa"), "aaaaa");
    }

    @Check("prefix doubling agrees with direct suffix sorting on 400 random strings")
    void doublingAgreesWithSorting() {
        RandomStrings strings = new RandomStrings(111);
        for (int trial = 0; trial < 400; trial++) {
            String s = strings.next(80, trial % 3 == 0 ? "a" : trial % 3 == 1 ? "ab" : "abcxyz");
            Expect.arrayEqual(SuffixArrays.bySortingSuffixes(s), SuffixArrays.byPrefixDoubling(s), s);
        }
    }
}
