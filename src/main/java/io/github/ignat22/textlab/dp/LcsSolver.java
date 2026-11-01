package io.github.ignat22.textlab.dp;

import java.util.Objects;

public final class LcsSolver {

    private LcsSolver() {
    }

    public static int[][] buildTable(String first, String second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        int rows = first.length();
        int cols = second.length();
        int[][] table = new int[rows + 1][cols + 1];
        for (int i = 1; i <= rows; i++) {
            char current = first.charAt(i - 1);
            int[] above = table[i - 1];
            int[] row = table[i];
            for (int j = 1; j <= cols; j++) {
                if (current == second.charAt(j - 1)) {
                    row[j] = above[j - 1] + 1;
                } else {
                    row[j] = Math.max(above[j], row[j - 1]);
                }
            }
        }
        return table;
    }

    public static int length(String first, String second) {
        return buildTable(first, second)[first.length()][second.length()];
    }

    public static LcsResult solve(String first, String second) {
        int[][] table = buildTable(first, second);
        StringBuilder reversed = new StringBuilder();
        int i = first.length();
        int j = second.length();
        while (i > 0 && j > 0) {
            if (first.charAt(i - 1) == second.charAt(j - 1)) {
                reversed.append(first.charAt(i - 1));
                i--;
                j--;
            } else if (table[i - 1][j] >= table[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        long cells = (long) first.length() * second.length();
        return new LcsResult(table[first.length()][second.length()], reversed.reverse().toString(), cells);
    }
}
