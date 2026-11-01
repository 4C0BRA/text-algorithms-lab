package io.github.ignat22.textlab.testkit;

import java.util.Arrays;
import java.util.Objects;

public final class Expect {

    private Expect() {
    }

    public static void equal(Object expected, Object actual, String context) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(context + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void equal(long expected, long actual, String context) {
        if (expected != actual) {
            throw new AssertionError(context + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void arrayEqual(int[] expected, int[] actual, String context) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(context + ": expected " + Arrays.toString(expected) + " but was " + Arrays.toString(actual));
        }
    }

    public static void that(boolean condition, String context) {
        if (!condition) {
            throw new AssertionError(context);
        }
    }

    public static <T extends Throwable> void fails(Class<T> type, Runnable action, String context) {
        try {
            action.run();
        } catch (Throwable thrown) {
            if (type.isInstance(thrown)) {
                return;
            }
            throw new AssertionError(context + ": expected " + type.getSimpleName() + " but got " + thrown, thrown);
        }
        throw new AssertionError(context + ": expected " + type.getSimpleName() + " but nothing was thrown");
    }
}
