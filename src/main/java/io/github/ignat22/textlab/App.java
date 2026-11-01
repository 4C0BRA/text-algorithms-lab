package io.github.ignat22.textlab;

import io.github.ignat22.textlab.bench.BenchmarkSuite;
import io.github.ignat22.textlab.dp.EditDistanceSolver;
import io.github.ignat22.textlab.dp.EditScript;
import io.github.ignat22.textlab.dp.EditStep;
import io.github.ignat22.textlab.dp.LcsResult;
import io.github.ignat22.textlab.dp.LcsSolver;
import io.github.ignat22.textlab.search.KmpSearcher;
import io.github.ignat22.textlab.search.NaiveSearcher;
import io.github.ignat22.textlab.search.PatternSearcher;
import io.github.ignat22.textlab.search.RabinKarpSearcher;
import io.github.ignat22.textlab.search.SearchReport;
import io.github.ignat22.textlab.suffix.KasaiLcp;
import io.github.ignat22.textlab.suffix.LcpResult;
import io.github.ignat22.textlab.suffix.SuffixArrays;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public final class App {

    private App() {
    }

    public static void main(String[] args) throws Exception {
        String command = args.length == 0 ? "demo" : args[0];
        switch (command) {
            case "demo" -> demo();
            case "bench" -> new BenchmarkSuite(Path.of("results"), Path.of("plots")).runAll();
            default -> {
                System.out.println("usage: java -jar text-algorithms-lab.jar [demo|bench]");
                System.exit(2);
            }
        }
    }

    private static void demo() {
        System.out.println("=== Longest Common Subsequence ===");
        LcsResult lcs = LcsSolver.solve("ABCBDAB", "BDCABA");
        System.out.println("ABCBDAB vs BDCABA -> length " + lcs.length() + ", one LCS: " + lcs.witness());

        System.out.println();
        System.out.println("=== Edit Distance ===");
        EditScript script = EditDistanceSolver.solve("kitten", "sitting");
        System.out.println("kitten -> sitting, distance " + script.distance());
        for (EditStep step : script.steps()) {
            System.out.println("  " + step);
        }

        System.out.println();
        System.out.println("=== String matching ===");
        String text = "abracadabra";
        String pattern = "abra";
        System.out.println("KMP border table of \"ababaca\": " + Arrays.toString(KmpSearcher.borderTable("ababaca")));
        for (PatternSearcher searcher : List.of(new NaiveSearcher(), new KmpSearcher(), new RabinKarpSearcher())) {
            SearchReport r = searcher.search(text, pattern);
            System.out.printf("  %-11s positions=%s comparisons=%d%n", r.algorithm(), r.positions(),
                    r.counters().totalComparisons());
        }

        System.out.println();
        System.out.println("=== Kasai LCP for \"abracadabra\" ===");
        int[] sa = SuffixArrays.byPrefixDoubling(text);
        LcpResult lcp = KasaiLcp.build(text, sa);
        System.out.println("  i  SA  LCP  suffix");
        for (int i = 0; i < sa.length; i++) {
            System.out.printf("%3d %3d %4d  %s%n", i, sa[i], lcp.lcp()[i], text.substring(sa[i]));
        }
        System.out.println("distinct substrings: " + lcp.distinctSubstrings());
    }
}
