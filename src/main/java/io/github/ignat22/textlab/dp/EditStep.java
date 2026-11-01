package io.github.ignat22.textlab.dp;

public record EditStep(Kind kind, char from, char to) {

    public enum Kind {
        KEEP, SUBSTITUTE, INSERT, DELETE
    }

    public boolean costsOne() {
        return kind != Kind.KEEP;
    }

    @Override
    public String toString() {
        return switch (kind) {
            case KEEP -> "keep '" + from + "'";
            case SUBSTITUTE -> "replace '" + from + "' with '" + to + "'";
            case INSERT -> "insert '" + to + "'";
            case DELETE -> "delete '" + from + "'";
        };
    }
}
