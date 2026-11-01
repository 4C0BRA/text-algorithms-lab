package io.github.ignat22.textlab.bench;

public record Measurement(double meanNanos, double stdevNanos, long minNanos, long maxNanos, int runs) {

    public double meanMicros() {
        return meanNanos / 1_000.0;
    }

    public double meanMillis() {
        return meanNanos / 1_000_000.0;
    }

    public double stdevMicros() {
        return stdevNanos / 1_000.0;
    }

    public double stdevMillis() {
        return stdevNanos / 1_000_000.0;
    }
}
