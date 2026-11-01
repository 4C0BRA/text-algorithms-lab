package io.github.ignat22.textlab.dp;

import io.github.ignat22.textlab.testkit.Check;
import io.github.ignat22.textlab.testkit.Expect;
import io.github.ignat22.textlab.testkit.RandomStrings;

public class EditDistanceTests {

    @Check("identical strings need 0 edits")
    void identicalStrings() {
        EditScript script = EditDistanceSolver.solve("levenshtein", "levenshtein");
        Expect.equal(0, script.distance(), "distance");
        Expect.equal(0L, script.editCount(), "edits in script");
    }

    @Check("one empty string: distance equals the other length")
    void oneEmpty() {
        Expect.equal(6, EditDistanceSolver.distance("", "string"), "insert everything");
        Expect.equal(6, EditDistanceSolver.distance("string", ""), "delete everything");
        Expect.equal(0, EditDistanceSolver.distance("", ""), "both empty");
    }

    @Check("only a substitution is needed: cat -> cut")
    void substitutionOnly() {
        EditScript script = EditDistanceSolver.solve("cat", "cut");
        Expect.equal(1, script.distance(), "distance");
        Expect.that(script.steps().stream().anyMatch(s -> s.kind() == EditStep.Kind.SUBSTITUTE), "has substitution");
    }

    @Check("only an insertion is needed: colr -> color")
    void insertionOnly() {
        EditScript script = EditDistanceSolver.solve("colr", "color");
        Expect.equal(1, script.distance(), "distance");
        Expect.that(script.steps().stream().anyMatch(s -> s.kind() == EditStep.Kind.INSERT), "has insertion");
    }

    @Check("only a deletion is needed: banana -> banna")
    void deletionOnly() {
        EditScript script = EditDistanceSolver.solve("banana", "banna");
        Expect.equal(1, script.distance(), "distance");
        Expect.that(script.steps().stream().anyMatch(s -> s.kind() == EditStep.Kind.DELETE), "has deletion");
    }

    @Check("mixed operations: kitten -> sitting = 3, sunday -> saturday = 3, intention -> execution = 5")
    void classicExamples() {
        Expect.equal(3, EditDistanceSolver.distance("kitten", "sitting"), "kitten/sitting");
        Expect.equal(3, EditDistanceSolver.distance("sunday", "saturday"), "sunday/saturday");
        Expect.equal(5, EditDistanceSolver.distance("intention", "execution"), "intention/execution");
    }

    @Check("the recovered script really turns source into target and has exactly d edits")
    void scriptIsExecutable() {
        RandomStrings strings = new RandomStrings(81);
        for (int trial = 0; trial < 300; trial++) {
            String a = strings.next(20, "abc");
            String b = strings.next(20, "abc");
            EditScript script = EditDistanceSolver.solve(a, b);
            Expect.equal(b, script.applyTo(a), "apply " + a + " -> " + b);
            Expect.equal(script.distance(), script.editCount(), "edit count for " + a + " -> " + b);
        }
    }

    @Check("metric properties: symmetry, |n-m| <= d <= max(n,m), triangle inequality")
    void metricProperties() {
        RandomStrings strings = new RandomStrings(82);
        for (int trial = 0; trial < 200; trial++) {
            String a = strings.next(15, "ab");
            String b = strings.next(15, "ab");
            String c = strings.next(15, "ab");
            int ab = EditDistanceSolver.distance(a, b);
            Expect.equal(ab, EditDistanceSolver.distance(b, a), "symmetry");
            Expect.that(ab >= Math.abs(a.length() - b.length()), "lower bound");
            Expect.that(ab <= Math.max(a.length(), b.length()), "upper bound");
            int ac = EditDistanceSolver.distance(a, c);
            int cb = EditDistanceSolver.distance(c, b);
            Expect.that(ab <= ac + cb, "triangle inequality");
        }
    }

    @Check("relation to LCS: d <= n + m - 2*LCS")
    void relationToLcs() {
        RandomStrings strings = new RandomStrings(83);
        for (int trial = 0; trial < 200; trial++) {
            String a = strings.next(25, "abcd");
            String b = strings.next(25, "abcd");
            int insertDeleteOnly = a.length() + b.length() - 2 * LcsSolver.length(a, b);
            Expect.that(EditDistanceSolver.distance(a, b) <= insertDeleteOnly, a + " / " + b);
        }
    }
}
