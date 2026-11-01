package io.github.ignat22.textlab.search;

import java.util.List;

public final class KmpSearcher extends PatternSearcher {

    @Override
    public String name() {
        return "KMP";
    }

    public static int[] borderTable(String pattern) {
        return borderTable(pattern, new SearchCounters());
    }

    static int[] borderTable(String pattern, SearchCounters counters) {
        int m = pattern.length();
        int[] border = new int[m];
        int width = 0;
        long comparisons = 0;
        for (int q = 1; q < m; q++) {
            char next = pattern.charAt(q);
            while (true) {
                comparisons++;
                if (pattern.charAt(width) == next) {
                    width++;
                    break;
                }
                if (width == 0) {
                    break;
                }
                width = border[width - 1];
            }
            border[q] = width;
        }
        counters.setupComparisons += comparisons;
        return border;
    }

    @Override
    protected void scan(String text, String pattern, List<Integer> positions, SearchCounters counters) {
        int[] border = borderTable(pattern, counters);
        int n = text.length();
        int m = pattern.length();
        int state = 0;
        long comparisons = 0;
        for (int i = 0; i < n; i++) {
            char current = text.charAt(i);
            while (true) {
                comparisons++;
                if (pattern.charAt(state) == current) {
                    state++;
                    break;
                }
                if (state == 0) {
                    break;
                }
                state = border[state - 1];
            }
            if (state == m) {
                positions.add(i - m + 1);
                state = border[m - 1];
            }
        }
        counters.scanComparisons += comparisons;
    }
}
