package io.github.ignat22.textlab.suffix;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public final class SuffixArrays {

    private SuffixArrays() {
    }

    public static int[] bySortingSuffixes(String s) {
        Objects.requireNonNull(s, "s");
        Integer[] order = new Integer[s.length()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (x, y) -> compareSuffixes(s, x, y));
        return Arrays.stream(order).mapToInt(Integer::intValue).toArray();
    }

    static int compareSuffixes(String s, int x, int y) {
        int n = s.length();
        while (x < n && y < n) {
            char a = s.charAt(x);
            char b = s.charAt(y);
            if (a != b) {
                return Character.compare(a, b);
            }
            x++;
            y++;
        }
        return Integer.compare(n - x, n - y);
    }

    public static int[] byPrefixDoubling(String s) {
        Objects.requireNonNull(s, "s");
        int n = s.length();
        if (n == 0) {
            return new int[0];
        }
        int[] rank = new int[n];
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            rank[i] = s.charAt(i);
            order[i] = i;
        }
        int[] nextRank = new int[n];
        for (int width = 1; ; width <<= 1) {
            final int step = width;
            final int[] current = rank;
            Comparator<Integer> byPair = Comparator
                    .<Integer>comparingInt(i -> current[i])
                    .thenComparingInt(i -> i + step < n ? current[i + step] : -1);
            Arrays.sort(order, byPair);
            nextRank[order[0]] = 0;
            for (int k = 1; k < n; k++) {
                int increment = byPair.compare(order[k - 1], order[k]) < 0 ? 1 : 0;
                nextRank[order[k]] = nextRank[order[k - 1]] + increment;
            }
            int[] swap = rank;
            rank = nextRank;
            nextRank = swap;
            if (rank[order[n - 1]] == n - 1 || step >= n) {
                break;
            }
        }
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = order[i];
        }
        return result;
    }
}
