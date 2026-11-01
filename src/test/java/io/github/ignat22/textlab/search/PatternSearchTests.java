package io.github.ignat22.textlab.search;

import io.github.ignat22.textlab.testkit.Check;
import io.github.ignat22.textlab.testkit.Expect;
import io.github.ignat22.textlab.testkit.RandomStrings;

import java.util.ArrayList;
import java.util.List;

public class PatternSearchTests {

    private static final List<PatternSearcher> SEARCHERS = List.of(
            new NaiveSearcher(),
            new KmpSearcher(),
            new RabinKarpSearcher()
    );

    private static void allFind(String text, String pattern, List<Integer> expected) {
        for (PatternSearcher searcher : SEARCHERS) {
            Expect.equal(expected, searcher.findAll(text, pattern),
                    searcher.name() + " on text=\"" + text + "\" pattern=\"" + pattern + "\"");
        }
    }

    @Check("pattern at the beginning of the text")
    void patternAtBeginning() {
        allFind("pattern matching", "pat", List.of(0));
    }

    @Check("pattern in the middle of the text")
    void patternInMiddle() {
        allFind("the quick brown fox", "brown", List.of(10));
    }

    @Check("pattern at the end of the text")
    void patternAtEnd() {
        allFind("rolling hash", "hash", List.of(8));
    }

    @Check("multiple separate occurrences")
    void multipleOccurrences() {
        allFind("abcXabcYabc", "abc", List.of(0, 4, 8));
    }

    @Check("overlapping occurrences are all reported")
    void overlappingOccurrences() {
        allFind("aaaaa", "aa", List.of(0, 1, 2, 3));
        allFind("abababab", "abab", List.of(0, 2, 4));
    }

    @Check("pattern absent from the text")
    void patternAbsent() {
        allFind("knuth morris pratt", "boyer", List.of());
    }

    @Check("pattern longer than the text gives no matches")
    void patternLongerThanText() {
        allFind("short", "much longer pattern", List.of());
    }

    @Check("pattern equal to the whole text")
    void patternIsWholeText() {
        allFind("exact", "exact", List.of(0));
    }

    @Check("empty pattern matches at every index 0..n (documented convention)")
    void emptyPattern() {
        allFind("abc", "", List.of(0, 1, 2, 3));
        allFind("", "", List.of(0));
    }

    @Check("empty text with a non-empty pattern gives no matches")
    void emptyText() {
        allFind("", "a", List.of());
    }

    @Check("single-character pattern")
    void singleCharacter() {
        allFind("mississippi", "s", List.of(2, 3, 5, 6));
    }

    @Check("all three algorithms agree with String.indexOf on 1500 random cases")
    void randomAgreement() {
        RandomStrings strings = new RandomStrings(91);
        for (int trial = 0; trial < 1500; trial++) {
            String alphabet = trial % 2 == 0 ? "ab" : "abcde";
            String text = strings.next(60, alphabet);
            String pattern = strings.next(6, alphabet);
            allFind(text, pattern, indexOfOracle(text, pattern));
        }
    }

    @Check("KMP border table for ababaca is 0 0 1 2 3 0 1")
    void borderTableExample() {
        Expect.arrayEqual(new int[]{0, 0, 1, 2, 3, 0, 1}, KmpSearcher.borderTable("ababaca"), "ababaca");
        Expect.arrayEqual(new int[]{0, 1, 2, 3}, KmpSearcher.borderTable("aaaa"), "aaaa");
        Expect.arrayEqual(new int[]{0, 0, 0, 1, 2, 3, 4, 5}, KmpSearcher.borderTable("abcabcab"), "abcabcab");
    }

    @Check("KMP never uses more than 2n comparisons in the scan and 2m in preprocessing")
    void kmpComparisonBound() {
        RandomStrings strings = new RandomStrings(92);
        KmpSearcher kmp = new KmpSearcher();
        for (int trial = 0; trial < 400; trial++) {
            String text = strings.next(300, "ab");
            String pattern = strings.next(12, "ab");
            SearchReport report = kmp.search(text, pattern);
            Expect.that(report.counters().scanComparisons() <= 2L * text.length(), "scan bound");
            Expect.that(report.counters().setupComparisons() <= 2L * pattern.length(), "setup bound");
        }
    }

    @Check("naive worst case does exactly (n-m+1)*m comparisons on aaaa..a / aa..ab")
    void naiveWorstCase() {
        String text = "a".repeat(100);
        String pattern = "a".repeat(9) + "b";
        SearchReport report = new NaiveSearcher().search(text, pattern);
        Expect.equal(91L * 10, report.counters().scanComparisons(), "comparisons");
    }

    @Check("null arguments are rejected")
    void nullArguments() {
        for (PatternSearcher searcher : SEARCHERS) {
            Expect.fails(NullPointerException.class, () -> searcher.search(null, "a"), searcher.name());
            Expect.fails(NullPointerException.class, () -> searcher.search("a", null), searcher.name());
        }
    }

    private static List<Integer> indexOfOracle(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        int from = 0;
        while (from <= text.length()) {
            int found = text.indexOf(pattern, from);
            if (found < 0) {
                break;
            }
            result.add(found);
            from = found + 1;
        }
        return result;
    }
}
