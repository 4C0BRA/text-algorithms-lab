package io.github.ignat22.textlab.bench;

import java.util.function.Supplier;

public final class Stopwatch {

    public static final int WARMUP_RUNS = 20;
    public static final int TIMED_RUNS = 5;

    public record Timed<T>(Measurement measurement, T result) {
    }

    private Stopwatch() {
    }

    public static <T> Timed<T> measure(Supplier<T> task) {
        T result = null;
        for (int i = 0; i < WARMUP_RUNS; i++) {
            result = task.get();
        }
        long[] samples = new long[TIMED_RUNS];
        for (int i = 0; i < TIMED_RUNS; i++) {
            long start = System.nanoTime();
            result = task.get();
            samples[i] = System.nanoTime() - start;
        }
        return new Timed<>(summarise(samples), result);
    }

    static Measurement summarise(long[] samples) {
        double sum = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (long sample : samples) {
            sum += sample;
            min = Math.min(min, sample);
            max = Math.max(max, sample);
        }
        double mean = sum / samples.length;
        double squares = 0;
        for (long sample : samples) {
            squares += (sample - mean) * (sample - mean);
        }
        double stdev = samples.length > 1 ? Math.sqrt(squares / (samples.length - 1)) : 0.0;
        return new Measurement(mean, stdev, min, max, samples.length);
    }
}
