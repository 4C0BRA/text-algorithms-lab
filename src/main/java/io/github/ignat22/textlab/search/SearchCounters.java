package io.github.ignat22.textlab.search;

public final class SearchCounters {

    long setupComparisons;
    long scanComparisons;
    long hashHits;
    long spuriousHits;

    public long setupComparisons() {
        return setupComparisons;
    }

    public long scanComparisons() {
        return scanComparisons;
    }

    public long totalComparisons() {
        return setupComparisons + scanComparisons;
    }

    public long hashHits() {
        return hashHits;
    }

    public long spuriousHits() {
        return spuriousHits;
    }
}
