package io.github.ignat22.textlab.testkit;

import io.github.ignat22.textlab.dp.EditDistanceTests;
import io.github.ignat22.textlab.dp.LcsTests;
import io.github.ignat22.textlab.search.PatternSearchTests;
import io.github.ignat22.textlab.search.RabinKarpTests;
import io.github.ignat22.textlab.suffix.SuffixArrayTests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class TestLauncher {

    private static final List<Class<?>> SUITES = List.of(
            LcsTests.class,
            EditDistanceTests.class,
            PatternSearchTests.class,
            RabinKarpTests.class,
            SuffixArrayTests.class
    );

    public static void main(String[] args) throws Exception {
        int passed = 0;
        List<String> failures = new ArrayList<>();
        for (Class<?> suite : SUITES) {
            System.out.println(suite.getSimpleName());
            Method[] methods = suite.getDeclaredMethods();
            Arrays.sort(methods, Comparator.comparing(Method::getName));
            for (Method method : methods) {
                Check check = method.getAnnotation(Check.class);
                if (check == null) {
                    continue;
                }
                String label = check.value().isEmpty() ? method.getName() : check.value();
                Object instance = suite.getDeclaredConstructor().newInstance();
                method.setAccessible(true);
                try {
                    method.invoke(instance);
                    passed++;
                    System.out.println("  [PASS] " + label);
                } catch (InvocationTargetException wrapped) {
                    Throwable cause = wrapped.getCause();
                    failures.add(suite.getSimpleName() + "." + method.getName() + " -> " + cause);
                    System.out.println("  [FAIL] " + label + " -> " + cause.getMessage());
                }
            }
        }
        System.out.println();
        System.out.println("Checks passed: " + passed + ", failed: " + failures.size());
        failures.forEach(failure -> System.out.println("  " + failure));
        if (!failures.isEmpty()) {
            System.exit(1);
        }
    }
}
