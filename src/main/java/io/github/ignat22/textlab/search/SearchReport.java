package io.github.ignat22.textlab.search;

import java.util.List;

public record SearchReport(String algorithm, List<Integer> positions, SearchCounters counters) {

    public int occurrences() {
        return positions.size();
    }
}
