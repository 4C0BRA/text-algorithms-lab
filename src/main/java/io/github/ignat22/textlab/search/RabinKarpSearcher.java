package io.github.ignat22.textlab.search;

import java.util.List;

public final class RabinKarpSearcher extends PatternSearcher {

    public static final long MERSENNE_61 = (1L << 61) - 1;
    public static final long DEFAULT_BASE = 1_000_003L;

    private final long base;
    private final long modulus;
    private final boolean mersenne;

    public RabinKarpSearcher() {
        this(DEFAULT_BASE, MERSENNE_61);
    }

    public RabinKarpSearcher(long base, long modulus) {
        if (modulus != MERSENNE_61 && (modulus < 2 || modulus > Integer.MAX_VALUE)) {
            throw new IllegalArgumentException("modulus must be 2^61-1 or in [2, 2^31-1]");
        }
        if (base < 1) {
            throw new IllegalArgumentException("base must be positive");
        }
        this.modulus = modulus;
        this.base = base % modulus;
        this.mersenne = modulus == MERSENNE_61;
    }

    @Override
    public String name() {
        return "Rabin-Karp";
    }

    public long modulus() {
        return modulus;
    }

    long multiply(long a, long b) {
        if (!mersenne) {
            return (a * b) % modulus;
        }
        long high = Math.multiplyHigh(a, b);
        long low = a * b;
        long folded = (low & MERSENNE_61) + (low >>> 61) + (high << 3);
        folded = (folded & MERSENNE_61) + (folded >>> 61);
        return folded >= MERSENNE_61 ? folded - MERSENNE_61 : folded;
    }

    private long add(long a, long b) {
        long sum = a + b;
        return sum >= modulus ? sum - modulus : sum;
    }

    private long subtract(long a, long b) {
        long difference = a - b;
        return difference < 0 ? difference + modulus : difference;
    }

    private long digit(char c) {
        return c % modulus;
    }

    public long hash(CharSequence s, int from, int length) {
        long h = 0;
        for (int i = from; i < from + length; i++) {
            h = add(multiply(h, base), digit(s.charAt(i)));
        }
        return h;
    }

    @Override
    protected void scan(String text, String pattern, List<Integer> positions, SearchCounters counters) {
        int n = text.length();
        int m = pattern.length();
        long leadingWeight = 1;
        for (int i = 1; i < m; i++) {
            leadingWeight = multiply(leadingWeight, base);
        }
        long patternHash = hash(pattern, 0, m);
        long windowHash = hash(text, 0, m);
        long comparisons = 0;
        long hits = 0;
        long spurious = 0;
        for (int shift = 0; ; shift++) {
            if (windowHash == patternHash) {
                hits++;
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
                } else {
                    spurious++;
                }
            }
            if (shift == n - m) {
                break;
            }
            long withoutLeading = subtract(windowHash, multiply(digit(text.charAt(shift)), leadingWeight));
            windowHash = add(multiply(withoutLeading, base), digit(text.charAt(shift + m)));
        }
        counters.scanComparisons += comparisons;
        counters.hashHits += hits;
        counters.spuriousHits += spurious;
    }
}
