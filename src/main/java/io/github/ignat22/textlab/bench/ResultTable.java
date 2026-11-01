package io.github.ignat22.textlab.bench;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ResultTable {

    private final List<String> columns;
    private final List<List<String>> rows = new ArrayList<>();

    public ResultTable(String... columns) {
        this.columns = List.of(columns);
    }

    public void row(Object... values) {
        if (values.length != columns.size()) {
            throw new IllegalArgumentException("expected " + columns.size() + " values, got " + values.length);
        }
        List<String> cells = new ArrayList<>();
        for (Object value : values) {
            cells.add(render(value));
        }
        rows.add(cells);
    }

    private static String render(Object value) {
        if (value instanceof Double d) {
            double abs = Math.abs(d);
            if (abs >= 100) {
                return String.format(Locale.ROOT, "%.1f", d);
            }
            if (abs >= 1) {
                return String.format(Locale.ROOT, "%.2f", d);
            }
            return String.format(Locale.ROOT, "%.3f", d);
        }
        return String.valueOf(value);
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder(String.join(",", columns)).append('\n');
        for (List<String> row : rows) {
            sb.append(String.join(",", row)).append('\n');
        }
        return sb.toString();
    }

    public String toMarkdown() {
        StringBuilder sb = new StringBuilder("| ").append(String.join(" | ", columns)).append(" |\n|");
        for (int i = 0; i < columns.size(); i++) {
            sb.append("---:|");
        }
        sb.append('\n');
        for (List<String> row : rows) {
            sb.append("| ").append(String.join(" | ", row)).append(" |\n");
        }
        return sb.toString();
    }

    public void writeCsv(Path file) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        Files.writeString(file, toCsv(), StandardCharsets.UTF_8);
    }
}
