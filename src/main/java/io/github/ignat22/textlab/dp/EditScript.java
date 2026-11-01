package io.github.ignat22.textlab.dp;

import java.util.List;

public record EditScript(int distance, List<EditStep> steps, long cellsFilled) {

    public long editCount() {
        return steps.stream().filter(EditStep::costsOne).count();
    }

    public String applyTo(String source) {
        StringBuilder out = new StringBuilder();
        int cursor = 0;
        for (EditStep step : steps) {
            switch (step.kind()) {
                case KEEP -> {
                    if (source.charAt(cursor) != step.from()) {
                        throw new IllegalStateException("script does not match source at " + cursor);
                    }
                    out.append(source.charAt(cursor++));
                }
                case SUBSTITUTE -> {
                    cursor++;
                    out.append(step.to());
                }
                case DELETE -> cursor++;
                case INSERT -> out.append(step.to());
            }
        }
        if (cursor != source.length()) {
            throw new IllegalStateException("script consumed " + cursor + " of " + source.length() + " characters");
        }
        return out.toString();
    }
}
