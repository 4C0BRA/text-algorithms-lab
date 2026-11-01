package io.github.ignat22.textlab.suffix;

import java.util.Objects;

public final class KasaiLcp {

    private KasaiLcp() {
    }

    public static LcpResult build(String s, int[] suffixArray) {
        Objects.requireNonNull(s, "s");
        Objects.requireNonNull(suffixArray, "suffixArray");
        int n = s.length();
        if (suffixArray.length != n) {
            throw new IllegalArgumentException("suffix array length " + suffixArray.length + " != string length " + n);
        }
        int[] rank = new int[n];
        boolean[] seen = new boolean[n];
        for (int position = 0; position < n; position++) {
            int start = suffixArray[position];
            if (start < 0 || start >= n || seen[start]) {
                throw new IllegalArgumentException("suffix array is not a permutation of 0.." + (n - 1));
            }
            seen[start] = true;
            rank[start] = position;
        }
        int[] lcp = new int[n];
        int carried = 0;
        long comparisons = 0;
        for (int i = 0; i < n; i++) {
            int position = rank[i];
            if (position == 0) {
                carried = 0;
                continue;
            }
            int previous = suffixArray[position - 1];
            while (i + carried < n && previous + carried < n) {
                comparisons++;
                if (s.charAt(i + carried) != s.charAt(previous + carried)) {
                    break;
                }
                carried++;
            }
            lcp[position] = carried;
            if (carried > 0) {
                carried--;
            }
        }
        return new LcpResult(lcp, comparisons);
    }
}
