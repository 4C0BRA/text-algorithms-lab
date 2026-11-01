package io.github.ignat22.textlab.dp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class EditDistanceSolver {

    private EditDistanceSolver() {
    }

    public static int[][] buildTable(String source, String target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        int rows = source.length();
        int cols = target.length();
        int[][] table = new int[rows + 1][cols + 1];
        for (int i = 0; i <= rows; i++) {
            table[i][0] = i;
        }
        for (int j = 0; j <= cols; j++) {
            table[0][j] = j;
        }
        for (int i = 1; i <= rows; i++) {
            char current = source.charAt(i - 1);
            int[] above = table[i - 1];
            int[] row = table[i];
            for (int j = 1; j <= cols; j++) {
                int replaceOrKeep = above[j - 1] + (current == target.charAt(j - 1) ? 0 : 1);
                int delete = above[j] + 1;
                int insert = row[j - 1] + 1;
                row[j] = Math.min(replaceOrKeep, Math.min(delete, insert));
            }
        }
        return table;
    }

    public static int distance(String source, String target) {
        return buildTable(source, target)[source.length()][target.length()];
    }

    public static EditScript solve(String source, String target) {
        int[][] table = buildTable(source, target);
        List<EditStep> steps = new ArrayList<>();
        int i = source.length();
        int j = target.length();
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0) {
                char from = source.charAt(i - 1);
                char to = target.charAt(j - 1);
                if (from == to && table[i][j] == table[i - 1][j - 1]) {
                    steps.add(new EditStep(EditStep.Kind.KEEP, from, to));
                    i--;
                    j--;
                    continue;
                }
                if (from != to && table[i][j] == table[i - 1][j - 1] + 1) {
                    steps.add(new EditStep(EditStep.Kind.SUBSTITUTE, from, to));
                    i--;
                    j--;
                    continue;
                }
            }
            if (i > 0 && table[i][j] == table[i - 1][j] + 1) {
                steps.add(new EditStep(EditStep.Kind.DELETE, source.charAt(i - 1), '\0'));
                i--;
            } else {
                steps.add(new EditStep(EditStep.Kind.INSERT, '\0', target.charAt(j - 1)));
                j--;
            }
        }
        Collections.reverse(steps);
        long cells = (long) source.length() * target.length();
        return new EditScript(table[source.length()][target.length()], List.copyOf(steps), cells);
    }
}
