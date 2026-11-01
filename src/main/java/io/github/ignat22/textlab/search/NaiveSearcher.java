package io.github.ignat22.textlab.search;

import java.util.List;

public final class NaiveSearcher extends PatternSearcher {

    @Override
    public String name() {
        return "Naive";
    }

    @Override
    protected void scan(String text, String pattern, List<Integer> positions, SearchCounters counters) {
        int n = text.length();
        int m = pattern.length();
        long comparisons = 0;
        for (int shift = 0; shift <= n - m; shift++) {
            int matched = 0;
            while (matched < m) {
                comparisons++;
                if (text.charAt(shift + matched) != pattern.charAt(matched)) {
                    break;
                }
                matched++;
            }
            if (matched == m) {
                positions.add(shift);
            }
        }
        counters.scanComparisons += comparisons;
    }
}
