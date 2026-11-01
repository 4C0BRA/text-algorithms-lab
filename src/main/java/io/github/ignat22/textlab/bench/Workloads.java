package io.github.ignat22.textlab.bench;

import java.util.SplittableRandom;

public final class Workloads {

    public static final long SEED = 2026L;
    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    private final SplittableRandom random;

    public Workloads(long seed) {
        this.random = new SplittableRandom(seed);
    }

    public String randomText(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public String patternAbsentFrom(String text, int length) {
        while (true) {
            String candidate = randomText(length);
            if (!text.contains(candidate)) {
                return candidate;
            }
        }
    }

    public static String fibonacciWord(int length) {
        String shorter = "a";
        String longer = "ab";
        while (longer.length() < length) {
            String next = longer + shorter;
            shorter = longer;
            longer = next;
        }
        return longer.substring(0, length);
    }
}
