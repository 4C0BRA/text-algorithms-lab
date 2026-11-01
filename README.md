# Text Algorithms Lab — Dynamic Programming and String Matching in Java

Assignment 4 · Design and Analysis of Algorithms

This repository implements, tests and benchmarks six classic text algorithms in plain Java 17:

| Part | Algorithms | Package |
|---|---|---|
| A. Dynamic programming | Longest Common Subsequence, Edit (Levenshtein) Distance | `io.github.ignat22.textlab.dp` |
| B. String matching | Naive, Knuth–Morris–Pratt, Rabin–Karp | `io.github.ignat22.textlab.search` |
| C. Advanced | Kasai's LCP array (plus a prefix-doubling suffix array used to feed it) | `io.github.ignat22.textlab.suffix` |

The project has **no external dependencies**: it compiles and runs with the JDK alone, including the test framework and the plots (SVG charts are produced by the benchmark itself).

**Quick start**

```bash
sh lab.sh test     # build + 47 automated checks
sh lab.sh demo     # small worked examples of every algorithm
sh lab.sh bench    # experiments -> results/*.csv, results/RESULTS.md, plots/*.svg
```

On Windows (PowerShell): `powershell -ExecutionPolicy Bypass -File lab.ps1 test` (also `demo`, `bench`, `all`).

---

## Contents

1. [Problem description and algorithm overview](#1-problem-description-and-algorithm-overview)
2. [Implementation decisions and assumptions](#2-implementation-decisions-and-assumptions)
3. [Complexity analysis](#3-complexity-analysis)
4. [Testing strategy and results](#4-testing-strategy-and-results)
5. [Experimental setup and timing methodology](#5-experimental-setup-and-timing-methodology)
6. [Results: tables and plots](#6-results-tables-and-plots)
7. [Theory versus experiment](#7-theory-versus-experiment)
8. [Conclusion: trade-offs](#8-conclusion-trade-offs)
9. [Build, run, repository layout and Git workflow](#9-build-run-repository-layout-and-git-workflow)

---

## 1. Problem description and algorithm overview

Text analysis tools (diff utilities, spell checkers, plagiarism detectors, search engines, DNA aligners) answer three kinds of questions:

* **How similar are two strings?** — LCS and Edit Distance (Part A).
* **Where does a pattern occur in a text?** — Naive, KMP, Rabin–Karp (Part B).
* **What repeated structure does one string contain?** — suffix array + LCP array (Part C).

### 1.1 Longest Common Subsequence

A *subsequence* keeps characters in order but may skip some (`ACE` is a subsequence of `ABCDE`). Given strings `X` (length n) and `Y` (length m), find the length of the longest string that is a subsequence of both.

Let `L[i][j]` be the LCS length of the prefixes `X[0..i)` and `Y[0..j)`.

```
L[0][j] = 0,  L[i][0] = 0                                   (an empty prefix shares nothing)
L[i][j] = L[i-1][j-1] + 1                if X[i-1] == Y[j-1]
L[i][j] = max(L[i-1][j], L[i][j-1])      otherwise
```

**Why the recurrence is correct.** Look at the last characters of the two prefixes.

* If they are equal, some optimal common subsequence ends with that character: if an optimal one did not use it, we could swap its last character for this pair without making it shorter. Removing the pair leaves an optimal solution for `(i-1, j-1)` — this is the *optimal substructure* property.
* If they differ, they cannot both be the last character of a common subsequence, so at least one of them is unused; the answer is the better of dropping `X[i-1]` or dropping `Y[j-1]`.

Every case refers only to strictly smaller prefixes, so filling the table row by row (top-down for `i`, left-to-right for `j`) always uses values that are already final. `L[n][m]` is the answer.

Worked example (`X = ABCB`, `Y = BDCAB`; this exact table is checked by a unit test):

|   | ε | B | D | C | A | B |
|---|---|---|---|---|---|---|
| ε | 0 | 0 | 0 | 0 | 0 | 0 |
| A | 0 | 0 | 0 | 0 | 1 | 1 |
| B | 0 | 1 | 1 | 1 | 1 | 2 |
| C | 0 | 1 | 1 | 2 | 2 | 2 |
| B | 0 | 1 | 1 | 2 | 2 | **3** |

The implementation also **reconstructs one LCS** (`LcsResult.witness`) by walking back from `L[n][m]`: on a match it takes the character and moves diagonally, otherwise it moves towards the larger neighbour (up on ties). For `ABCBDAB / BDCABA` it returns `BCBA` (length 4).

### 1.2 Edit Distance (Levenshtein)

The minimum number of single-character **insertions, deletions and substitutions** that turn `S` (length n) into `T` (length m). Let `D[i][j]` be the distance between `S[0..i)` and `T[0..j)`.

```
D[i][0] = i                     (delete all i characters)
D[0][j] = j                     (insert all j characters)
D[i][j] = min( D[i-1][j-1] + (S[i-1] == T[j-1] ? 0 : 1),    keep or substitute
               D[i-1][j]   + 1,                             delete S[i-1]
               D[i][j-1]   + 1 )                            insert T[j-1]
```

**Why it is correct.** Consider an optimal alignment of the two prefixes and its *last column*. There are exactly three possibilities: `S[i-1]` is aligned with `T[j-1]` (cost 0 if equal, 1 for a substitution), `S[i-1]` is deleted, or `T[j-1]` is inserted. Whatever happens in the last column, the rest of the alignment must be an optimal alignment of the remaining prefixes (otherwise replacing it with a cheaper one would improve the whole). Taking the minimum over the three cases therefore gives the optimum. The base cases are forced: transforming into or from an empty string can only insert or delete.

Besides the number, `EditDistanceSolver.solve` returns an **edit script** — the list of operations recovered by backtracking through the table. The script is executable (`EditScript.applyTo(source)` produces the target), which is how the tests verify the reconstruction. Example from `demo`:

```
kitten -> sitting, distance 3
  replace 'k' with 's', keep 'i', keep 't', keep 't', replace 'e' with 'i', keep 'n', insert 'g'
```

### 1.3 Naive string matching

Try every shift `s = 0 … n−m` and compare `pattern[0..m)` with `text[s..s+m)` left to right, stopping at the first mismatch. Simple, no preprocessing, and the baseline for the other two algorithms.

### 1.4 Knuth–Morris–Pratt

KMP precomputes the **border table** (also called prefix function / LPS table): `border[q]` is the length of the longest proper prefix of `pattern[0..q]` that is also a suffix of it. For `ababaca` the table is `0 0 1 2 3 0 1`.

During the scan KMP keeps `state` = number of pattern characters currently matched.

* On a match: `state++`. If `state == m`, report `i − m + 1` and continue from `state = border[m−1]` (so overlapping matches are found).
* On a mismatch with `state > 0`: fall back to `state = border[state−1]` and compare the same text character again.
* On a mismatch with `state == 0`: move to the next text character.

**How KMP avoids repeated comparisons.** When a mismatch happens after `q` matched characters, the last `q` text characters are already known — they equal `pattern[0..q)`. The naive algorithm would forget this, shift by one and re-read most of them. KMP instead shifts the pattern so that its longest border lines up with the end of the matched part; every shift in between is provably impossible (it would require a longer border). As a result **the text index never moves backwards**, and each text character is read once on success, plus once per fallback.

### 1.5 Rabin–Karp

Rabin–Karp compares **numbers instead of strings**. A window `c₀c₁…c_{m−1}` gets the polynomial hash

```
h = (c₀·B^(m−1) + c₁·B^(m−2) + … + c_{m−1}) mod q
```

and the next window's hash is obtained in O(1) by a **rolling update**:

```
h' = ((h − c_out · B^(m−1)) · B + c_in) mod q
```

Implementation choices (different from the textbook `mod 101` example):

* modulus `q = 2^61 − 1` (a Mersenne prime) and base `B = 1,000,003`;
* the 122-bit product `a·b` is reduced without division: `Math.multiplyHigh` gives the high 64 bits, and because `2^61 ≡ 1 (mod q)` the reduction is just shifts, masks and additions;
* a constructor `RabinKarpSearcher(base, modulus)` accepts any modulus up to 2³¹−1, which the tests use to *force* collisions.

**How hash collisions are handled.** Equal hashes do not prove equal strings. Every *hash hit* is verified character by character; only a full match is reported. A hit that fails verification is a *spurious hit* and is counted in `SearchCounters.spuriousHits`. Correctness therefore never depends on the hash; only the running time does. With `q ≈ 2.3·10¹⁸` the expected number of spurious hits in a 100,000-character text is about `10⁵ / q ≈ 4·10⁻¹⁴` — and indeed it was 0 in every experiment. With modulus 7 in the tests there are thousands of spurious hits, and the output is still exactly the same as Naive's.

### 1.6 Kasai's algorithm for the LCP array

The **suffix array** `SA` of a string `s` lists the starting positions of all suffixes in lexicographic order. The **LCP array** stores, for every position `i ≥ 1` of the suffix array, the length of the longest common prefix of the suffixes `SA[i−1]` and `SA[i]` (neighbours in sorted order). By convention `LCP[0] = 0`.

**What the LCP array represents.** Because sorted order groups suffixes that start the same way, *every repeated substring shows up as a common prefix of two neighbouring suffixes*. Hence:

* `max(LCP)` = length of the longest repeated substring (`abra` in `abracadabra`, length 4);
* number of distinct substrings = `n(n+1)/2 − Σ LCP` (54 for `abracadabra`);
* together with `SA` it supports pattern search, longest common substring of two texts, etc.

Hand-verified example used in the tests (`s = abracadabra`, n = 11):

| i | SA[i] | suffix | LCP[i] | common prefix with the previous suffix |
|---:|---:|---|---:|---|
| 0 | 10 | a | 0 | — |
| 1 | 7 | abra | 1 | `a` |
| 2 | 0 | abracadabra | 4 | `abra` |
| 3 | 3 | acadabra | 1 | `a` |
| 4 | 5 | adabra | 1 | `a` |
| 5 | 8 | bra | 0 | — |
| 6 | 1 | bracadabra | 3 | `bra` |
| 7 | 4 | cadabra | 0 | — |
| 8 | 6 | dabra | 0 | — |
| 9 | 9 | ra | 0 | — |
| 10 | 2 | racadabra | 2 | `ra` |

**Kasai's idea.** Computing each LCP from scratch costs up to O(n) per pair, O(n²) in total. Kasai processes suffixes in **text order** `i = 0, 1, …, n−1` (not in suffix-array order) and keeps a counter `h`. Key lemma: if the suffix at `i` shares `h > 0` characters with its predecessor in `SA`, then the suffix at `i+1` shares **at least `h − 1`** characters with *its* predecessor (drop the first character of both suffixes; the shorter pair stays sorted and still shares `h−1` characters, and the real predecessor of `i+1` is between them in sorted order). So after each step the algorithm only decreases `h` by one and continues comparing from there instead of from zero.

```
rank[SA[p]] = p                      for all p
h = 0
for i in 0..n-1:
    p = rank[i]
    if p == 0: h = 0; continue
    j = SA[p-1]
    while i+h < n and j+h < n and s[i+h] == s[j+h]: h++
    LCP[p] = h
    if h > 0: h--
```

The suffix array itself is outside the scope of the assignment; `SuffixArrays.byPrefixDoubling` (O(n log² n)) builds it for the experiments and `SuffixArrays.bySortingSuffixes` (direct comparison sort) is kept as an independent cross-check in the tests. Neither is timed.

---

## 2. Implementation decisions and assumptions

**Code structure**

* `dp` — `LcsSolver` and `EditDistanceSolver` are static, stateless utilities. Each exposes the raw table (`buildTable`), the number only (`length` / `distance`, used for timing) and a rich result (`solve`) with reconstruction: `LcsResult(length, witness, cellsFilled)` and `EditScript(distance, steps, cellsFilled)`.
* `search` — the three matchers extend one abstract class `PatternSearcher`. It is a *template method*: the public `search(text, pattern)` validates input and handles the edge cases **once**, then calls the algorithm-specific `scan(...)`. This guarantees that all three algorithms follow identical conventions. The result is a `SearchReport(algorithm, positions, counters)`.
* `suffix` — `KasaiLcp.build(s, suffixArray)` returns `LcpResult(lcp, comparisons)`; `LcpResult` also derives `max()`, `average()` and `distinctSubstrings()`.
* `bench` — seeded workload generator, stopwatch, CSV/Markdown table writer and the experiment suite.
* `chart` — a small SVG line-chart renderer, so the plots are regenerated by `bench` with no Python or plotting library.
* Source code intentionally contains no comments; names are chosen to be self-explanatory and this README carries the full explanation.

**Conventions and assumptions**

| Situation | Behaviour | Reason |
|---|---|---|
| Empty pattern | matches at **every index 0…n** (n+1 positions); for an empty text the answer is `[0]` | the empty string is a prefix of every suffix, including the empty suffix at index n; consistent with `"abc".indexOf("", k) == k` |
| Pattern longer than text | empty list, no comparisons | no window of length m exists |
| Overlapping matches | all reported (`aa` in `aaaaa` → `0,1,2,3`) | standard definition of "all occurrences" |
| Indices | 0-based starting positions, ascending | |
| `null` arguments | `NullPointerException` | fail fast instead of silently returning nothing |
| Characters | Java `char` (UTF-16 code unit); any alphabet works | hashing uses the char code as the digit |
| LCS witness | one LCS is returned (ties broken by moving up) | LCS is not unique; the length is |
| Edit operations | unit cost for insert, delete, substitute | Levenshtein definition |
| `LCP[0]` | 0 | no predecessor |
| Invalid suffix array | `IllegalArgumentException` (wrong length, duplicate, out of range) | Kasai's correctness depends on the input being a permutation |
| Operation counting | every algorithm counts its character comparisons in a local `long` and stores it once | negligible and *equal* overhead for all algorithms |

**Operation counters**

* Naive / KMP / Rabin–Karp: *scan comparisons* (pattern vs text characters) and *setup comparisons* (KMP border table). Rabin–Karp also reports *hash hits* and *spurious hits*; its scan comparisons are verification comparisons only.
* LCS / Edit Distance: *cells filled* (= n·m).
* Kasai: character comparisons inside the `while` loop.

---

## 3. Complexity analysis

Notation: n = text (or first string) length, m = pattern (or second string) length, k = number of occurrences, σ = alphabet size. *Auxiliary space* excludes the input strings and the output list of positions (O(k)).

| Algorithm | Main operation / recurrence | Best-case time | Worst-case time | Auxiliary space | Justification |
|---|---|---|---|---|---|
| **LCS** | `L[i][j] = L[i−1][j−1]+1` on match, else `max(L[i−1][j], L[i][j−1])` | Θ(n·m) | Θ(n·m) | Θ(n·m) | All (n+1)(m+1) cells are filled once, O(1) each, regardless of content. Reconstruction adds O(n+m). |
| **Edit Distance** | `D[i][j] = min(D[i−1][j−1]+[≠], D[i−1][j]+1, D[i][j−1]+1)` | Θ(n·m) | Θ(n·m) | Θ(n·m) | Same table shape; each cell is a minimum of three values. Base row/column O(n+m); backtracking O(n+m). |
| **Naive** | compare `pattern[j]` with `text[s+j]` for each shift `s` | Θ(n) — first character mismatches at every shift | Θ((n−m+1)·m) = O(n·m) — e.g. `aaaa…a` / `aa…ab` | Θ(1) | n−m+1 shifts, each costing between 1 and m comparisons; no preprocessing. |
| **KMP — preprocessing** | border table: extend `width` or fall back to `border[width−1]` | Θ(m) | Θ(m) | Θ(m) | `width` grows by ≤ 1 per step and every fallback decreases it, so the total number of comparisons is < 2m. |
| **KMP — search** | match → `state++`; mismatch → `state = border[state−1]` | Θ(n) | Θ(n) | Θ(m) for the table | The text index only advances. Every comparison either advances `i` (≤ n times) or decreases `state`, which can drop at most as many times as it grew (≤ n). Total ≤ 2n comparisons; overall **Θ(n + m)**. |
| **Rabin–Karp — preprocessing** | Horner hash of pattern and first window, `B^(m−1)` | Θ(m) | Θ(m) | Θ(1) | One pass over m characters, O(1) modular work each. |
| **Rabin–Karp — search** | rolling hash; verify on hash hit | Θ(n) — no hash hits | Θ((n−m+1)·m) = O(n·m) | Θ(1) | n−m+1 O(1) roll updates, plus up to m comparisons per hit. **Expected Θ(n + m + k·m)** for a random-like hash: spurious hits ≈ (n−m+1)/q. **Worst case O(n·m)** when almost every window hits: many real matches (`aaaa…` / `aaa`) or a bad modulus / adversarial input. |
| **Kasai** | `h` = LCP with SA-predecessor, reused as `h − 1` for the next text position | Θ(n) | Θ(n) | Θ(n) — `rank` array (+ n-int output) | `h ≤ n` and decreases by at most 1 per iteration (≤ n decrements), so it increases at most 2n times; plus ≤ n mismatching comparisons → **≤ 3n comparisons**. Building `rank` is Θ(n). |
| *Suffix array (helper)* | prefix doubling: sort by (rank[i], rank[i+2ᵏ]) | Θ(n log n) | Θ(n log² n) | Θ(n) | ⌈log n⌉ rounds, each an O(n log n) comparison sort. Not part of the timed experiments. |

**Theoretical complexity vs measured time.** The table above counts abstract steps. Measured wall-clock time also includes the cost per step (a modular multiplication is more expensive than a `char` comparison), memory and cache behaviour, branch prediction and JIT compilation. Section 7 compares both: operation counts are expected to match theory almost exactly, while times only follow the *growth rate*.

**Expected vs worst case.** Only Rabin–Karp (and Naive, on random input) has a gap between the typical and the worst case:

* *Rabin–Karp*: the O(n + m) bound is an **expectation** over the behaviour of the hash; a guaranteed bound does not exist because collisions cannot be excluded. The worst case is reached by input with many real occurrences (verification cost k·m) or by a weak modulus.
* *Naive*: on random text over σ letters the expected number of comparisons per shift is `1 + 1/σ + 1/σ² + … < σ/(σ−1)`, i.e. ≈ 1.04 for σ = 26, so the **expected** time is Θ(n). The worst case Θ(n·m) appears only on repetitive text.

KMP, LCS, Edit Distance and Kasai have no such gap: their best and worst cases coincide asymptotically.

**Space optimisation (not used).** LCS and Edit Distance values need only the previous row, so O(min(n, m)) space is possible. This implementation keeps the full table on purpose, because the assignment asks for a DP table and because the full table is required to reconstruct the LCS and the edit script.

---

## 4. Testing strategy and results

The tests live in `src/test/java`. A tiny self-written framework (`testkit`: `@Check` annotation, `Expect` assertions and a reflective `TestLauncher`) keeps the project dependency-free; the launcher prints PASS/FAIL per check and exits with code 1 on any failure.

Three kinds of checks are used:

1. **Required edge cases** with hand-computed answers.
2. **Cross-implementation agreement**: every string-matching check runs **all three** algorithms through one helper (`allFind`) and requires the same list of positions.
3. **Randomised oracles** with fixed seeds: a memoised top-down LCS, `String.indexOf` for matching, a brute-force LCP, `BigInteger` for Mersenne arithmetic, and direct suffix sorting for the suffix array. (The oracles use library methods only inside tests, which the assignment allows.)

| Suite | Checks | What is covered |
|---|---:|---|
| `LcsTests` | 8 | identical strings, completely different strings, empty string (each side and both), CLRS example, full hand-computed DP table, symmetry and bounds, 300 random pairs vs top-down recursion (witness checked to be a common subsequence of correct length), `null` |
| `EditDistanceTests` | 9 | identical strings, one empty string (both directions), substitution only (`cat→cut`), insertion only (`colr→color`), deletion only (`banana→banna`), mixed classics (`kitten→sitting` 3, `sunday→saturday` 3, `intention→execution` 5), executable edit script on 300 random pairs, metric properties incl. triangle inequality, relation `d ≤ n+m−2·LCS` |
| `PatternSearchTests` | 16 | pattern at beginning / middle / end, multiple occurrences, overlapping occurrences, pattern absent, **pattern longer than text**, pattern = text, **empty pattern**, empty text, single character, 1500 random cases vs `indexOf`, KMP border tables, KMP ≤ 2n / 2m comparison bounds, exact naive worst case `(n−m+1)·m`, `null` |
| `RabinKarpTests` | 5 | Mersenne multiplication vs `BigInteger` (5000 pairs), rolling hash finds exactly one window, **forced collisions** with modulus 7 (> 1000 spurious hits, results still correct, `hits = matches + spurious`), zero spurious hits with 2⁶¹−1 on 100k characters, invalid modulus |
| `SuffixArrayTests` | 3 | hand-sorted `abracadabra`, edge cases, prefix doubling vs direct sorting on 400 random strings |
| `KasaiTests` | 6 | **manually verified `abracadabra`** SA → LCP `0 1 4 1 1 0 3 0 0 0 2` (also max = 4, distinct substrings = 54), `aaaa`, empty and one-character strings, 400 random strings vs brute force, ≤ 3n comparisons, invalid suffix arrays |

**Result:** `Checks passed: 47, failed: 0`.

```
$ sh lab.sh test
...
KasaiTests
  [PASS] abracadabra: manually verified SA gives LCP 0 1 4 1 1 0 3 0 0 0 2
  ...
Checks passed: 47, failed: 0
```

In addition, the benchmark itself re-checks correctness: for every text size and pattern, KMP and Rabin–Karp must return exactly the same positions as Naive, otherwise the run aborts.

---

## 5. Experimental setup and timing methodology

**Workloads** (all from `bench/Workloads.java`):

| Experiment | Input sizes | Inputs |
|---|---|---|
| LCS | two strings of length 100, 500, 1,000 | independent random strings |
| Edit Distance | two strings of length 100, 500, 1,000 | **the same** strings as LCS |
| String matching, random | text 10,000, 50,000, 100,000; m = 16 | random text; *present* pattern = `text[n/2 .. n/2+16)`; *absent* pattern = random 16 letters not occurring in the text |
| String matching, structured | text 10,000, 50,000, 100,000; m = 40 | **Fibonacci word** over {a, b} (`abaababaabaab…`); *present* = its first 40 characters; *absent* = first 38 characters + `bb` (a Fibonacci word never contains `bb`) |
| Kasai | 1,000, 5,000, 10,000 | random string and Fibonacci word |

* **Alphabet** for random strings: the 26 lowercase English letters `abcdefghijklmnopqrstuvwxyz`, uniform.
* **Seed**: `java.util.SplittableRandom` with seed **2026** (LCS/ED), 2027 (matching), 2028 (Kasai), so each experiment is reproducible on its own.
* The structured Fibonacci input was added because uniform random text over 26 letters is the *easiest* possible case for Naive. The Fibonacci word is highly repetitive (it has lots of long repeated prefixes), so it exposes the difference between algorithms that re-read the text and KMP.
* Naive, KMP and Rabin–Karp always receive **the identical text and pattern objects**; LCS and Edit Distance receive identical string pairs.

**Timing methodology**

* `System.nanoTime()` is read immediately before and after **only the algorithm call** (`Stopwatch.measure`). Input generation, suffix-array construction, correctness cross-checks, console output and file writing are all outside the timed region.
* JIT warm-up: a global warm-up pass (100 iterations of every algorithm on random and Fibonacci inputs), then **20 untimed warm-up runs** before each measurement.
* Each measurement is **5 timed runs**; the tables report the **mean** and the sample standard deviation.
* The timed call returns its result, which is kept, so the JIT cannot eliminate the work as dead code.
* For timing, LCS and Edit Distance are called through `length` / `distance` (table construction only, the asymptotically relevant part); reconstruction is tested separately.
* Machine for the numbers below: Linux x86-64 virtual machine, 2 logical CPUs, OpenJDK 21 running code compiled with `--release 17`. Absolute numbers will differ on other machines; the trends should not.

---

## 6. Results

To be added after the experiments are analysed.
