package io.github.ignat22.textlab.suffix;

public record LcpResult(int[] lcp, long comparisons) {

    public int max() {
        int best = 0;
        for (int value : lcp) {
            best = Math.max(best, value);
        }
        return best;
    }

    public double average() {
        if (lcp.length < 2) {
            return 0.0;
        }
        long sum = 0;
        for (int value : lcp) {
            sum += value;
        }
        return (double) sum / (lcp.length - 1);
    }

    public long distinctSubstrings() {
        long n = lcp.length;
        long sum = 0;
        for (int value : lcp) {
            sum += value;
        }
        return n * (n + 1) / 2 - sum;
    }
}
