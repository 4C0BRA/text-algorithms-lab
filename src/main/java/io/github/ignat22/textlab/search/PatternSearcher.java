package io.github.ignat22.textlab.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class PatternSearcher {

    public final SearchReport search(String text, String pattern) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(pattern, "pattern");
        SearchCounters counters = new SearchCounters();
        List<Integer> positions = new ArrayList<>();
        if (pattern.isEmpty()) {
            for (int i = 0; i <= text.length(); i++) {
                positions.add(i);
            }
        } else if (pattern.length() <= text.length()) {
            scan(text, pattern, positions, counters);
        }
        return new SearchReport(name(), Collections.unmodifiableList(positions), counters);
    }

    public List<Integer> findAll(String text, String pattern) {
        return search(text, pattern).positions();
    }

    public abstract String name();

    protected abstract void scan(String text, String pattern, List<Integer> positions, SearchCounters counters);
}
