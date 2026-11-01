package io.github.ignat22.textlab.testkit;

import java.util.SplittableRandom;

public final class RandomStrings {

    private final SplittableRandom random;

    public RandomStrings(long seed) {
        this.random = new SplittableRandom(seed);
    }

    public String next(int maxLength, String alphabet) {
        int length = random.nextInt(maxLength + 1);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
