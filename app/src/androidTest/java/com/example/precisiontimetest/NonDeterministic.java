package com.example.precisiontimetest;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.text.DecimalFormat;
import java.util.StringJoiner;

/**
 * A tool to help manage "flaky" automated tests - which produce inconsistent results based on
 * factors outside of the tests control. The primary purpose is to configure certain tests to
 * execute repeatedly in order to make a more reliable determination about pass vs. fail.
 * <p>
 * This is done by executing the flaky test N times (where N > 1). The outcome would be failure only
 * if N/N attempts result in failure. In all other cases, such a test would pass. Some examples to
 * illustrate the concept:
 * <p>
 * 1. fail, fail, pass = pass
 * 2. pass, fail, pass = pass
 * 3. fail, fail, fail = fail
 * <p>
 * Note that this implementation is inspired by an article referenced below.
 * Ref: https://testing.googleblog.com/2016/05/flaky-tests-at-google-and-how-we.html
 * <p>
 * Practical advice and a warning
 * 1. Ideally this is used as a temporary measure so that developers can continue receiving fast
 * feedback. After that, prioritize efforts to identify and eliminate the source of undesired
 * variation (i.e. factors outside of the test, unintended variability, etc.)
 * 2. There may be some temptation to use this liberally throughout a collection of tests. That
 * would be a misuse of the tool and a bad process smell which leads to wasted resources and
 * increased total cost of ownership - among other undesirable side-effects
 * <p>
 * Usage:
 * <pre>
 * &#064;Rule public NonDeterministic nonDeterministic = new NonDeterministic();
 *
 * &#064;Flaky
 * &#064;Test public void fetchProfileWithinOneSecond() {
 *     //Given: Unknowable network conditions which affect timing
 *     //When: Fetch profile from remote server
 *     final long startTime = System.nanoTime();
 *     fetchProfile(); //Blocking
 *     //Then: Complete the operation within one second
 *     final long completionTime = System.nanoTime() - startTime;
 *     assertTrue(completionTime < 1_000_000_000);
 * }
 * </pre>
 */

public class NonDeterministic implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description) {
        return new Statement() {

            private final StringJoiner results = new StringJoiner("\n");
            private Throwable lastException = new UnknownError();

            @Override
            public void evaluate() throws Throwable {
                Flaky f = description.getAnnotation(Flaky.class);
                if (f != null) {
                    //Get the parameters
                    final int requiredIterations = f.iterations();
                    final boolean traceAllFailures = f.traceAllFailures();
                    final boolean summarizeIterations = f.itemizeSummary();

                    //Run the tests
                    final int failures = iterate(requiredIterations, summarizeIterations, traceAllFailures);
                    logTestExecutionSummary(failures, requiredIterations, summarizeIterations);

                    //Only fail if all tests fail
                    if (failures == requiredIterations) {
                        throw lastException;
                    }
                } else {
                    base.evaluate();
                }
            }

            private int iterate(final int requiredIterations, final boolean summarizeIterations, final boolean traceAllFailures) {
                int failures = 0;

                for (int i = 0; i < requiredIterations; i++) {
                    String attemptCount = getHumanReadableAttemptCount(i, requiredIterations);
                    try {
                        base.evaluate();
                        handleTestPass(attemptCount);
                    } catch (Throwable e) {
                        failures++;
                        handleTestFailure(attemptCount, e, summarizeIterations, traceAllFailures);
                    }
                }

                return failures;
            }

            private void logTestExecutionSummary(final int failures, final int iterations, final boolean summarizeIterations) {
                float failureRate = (float) failures / iterations;
                String frLabel = new DecimalFormat("##%").format(failureRate);
                String summary = String.format("Flaky Test Summary for %s (%s failure rate)",
                        description.getDisplayName(),
                        frLabel);
                log("\n\n"
                        + summary
                        + ((summarizeIterations) ? "\n" + results.toString() : "")
                        + "\n\n");
            }

            private void handleTestPass(final String iteration){
                String oneLiner = String.format("Flaky Test Iteration %s passed", iteration);
                log(oneLiner);
                results.add(oneLiner);
            }

            private void handleTestFailure(final String iteration, final Throwable t, final boolean summarizeIterations, final boolean traceAllFailures) {
                lastException = t;
                String oneLiner = String.format("Flaky Test Iteration %s failed", iteration);
                if(traceAllFailures){
                    new Throwable(oneLiner, lastException).printStackTrace();
                } else {
                    log(oneLiner + ": " + lastException.getMessage());
                }
                if(summarizeIterations) {
                    results.add(oneLiner);
                }
            }

            private void log(final String message) {
                System.out.println(message);
            }

            private String getHumanReadableAttemptCount(final int index, final int requiredIterations) {
                return (index + 1) + "/" + requiredIterations;
            }
        };
    }
}