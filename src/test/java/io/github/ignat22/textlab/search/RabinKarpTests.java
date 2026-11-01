package io.github.ignat22.textlab.search;

import io.github.ignat22.textlab.testkit.Check;
import io.github.ignat22.textlab.testkit.Expect;
import io.github.ignat22.textlab.testkit.RandomStrings;

import java.math.BigInteger;
import java.util.List;
import java.util.SplittableRandom;

public class RabinKarpTests {

    @Check("Mersenne 2^61-1 multiplication agrees with BigInteger")
    void mersenneMultiplication() {
        RabinKarpSearcher searcher = new RabinKarpSearcher();
        BigInteger modulus = BigInteger.valueOf(RabinKarpSearcher.MERSENNE_61);
        SplittableRandom random = new SplittableRandom(101);
        for (int trial = 0; trial < 5000; trial++) {
            long a = random.nextLong(RabinKarpSearcher.MERSENNE_61);
            long b = random.nextLong(RabinKarpSearcher.MERSENNE_61);
            long expected = BigInteger.valueOf(a).multiply(BigInteger.valueOf(b)).mod(modulus).longValue();
            Expect.equal(expected, searcher.multiply(a, b), a + " * " + b);
        }
        long top = RabinKarpSearcher.MERSENNE_61 - 1;
        Expect.equal(1L, searcher.multiply(top, top), "(p-1)^2 mod p");
    }

    @Check("rolling hash of every window equals the hash computed from scratch")
    void rollingMatchesDirectHash() {
        RabinKarpSearcher searcher = new RabinKarpSearcher(257, 1_000_000_007L);
        String text = "rolling hashes slide one character at a time";
        int m = 7;
        SearchReport report = searcher.search(text, text.substring(13, 13 + m));
        Expect.equal(List.of(13), report.positions(), "only one window matches");
        Expect.equal(1L, report.counters().hashHits(), "single hash hit");
    }

    @Check("tiny modulus 7 forces collisions, yet results stay correct")
    void collisionsAreVerified() {
        RabinKarpSearcher weak = new RabinKarpSearcher(256, 7);
        RandomStrings strings = new RandomStrings(102);
        long spuriousTotal = 0;
        for (int trial = 0; trial < 400; trial++) {
            String text = strings.next(80, "abcd");
            String pattern = strings.next(5, "abcd");
            if (pattern.isEmpty()) {
                continue;
            }
            SearchReport weakReport = weak.search(text, pattern);
            Expect.equal(new NaiveSearcher().findAll(text, pattern), weakReport.positions(), text + " / " + pattern);
            Expect.equal(weakReport.counters().hashHits() - weakReport.occurrences(),
                    weakReport.counters().spuriousHits(), "hits = real + spurious");
            spuriousTotal += weakReport.counters().spuriousHits();
        }
        Expect.that(spuriousTotal > 1000, "expected many spurious hits, got " + spuriousTotal);
    }

    @Check("default 2^61-1 modulus gives no spurious hits on 100k random characters")
    void strongModulusHasNoSpuriousHits() {
        SplittableRandom random = new SplittableRandom(103);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100_000; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        String text = sb.toString();
        SearchReport report = new RabinKarpSearcher().search(text, text.substring(50_000, 50_008));
        Expect.equal(0L, report.counters().spuriousHits(), "spurious hits");
        Expect.that(report.occurrences() >= 1, "pattern taken from the text must be found");
    }

    @Check("invalid modulus is rejected")
    void invalidModulus() {
        Expect.fails(IllegalArgumentException.class, () -> new RabinKarpSearcher(31, 1), "modulus 1");
        Expect.fails(IllegalArgumentException.class, () -> new RabinKarpSearcher(31, 1L << 40), "modulus 2^40");
    }
}
