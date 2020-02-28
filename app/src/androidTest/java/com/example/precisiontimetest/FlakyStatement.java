package com.example.precisiontimetest;

import androidx.annotation.NonNull;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.text.DecimalFormat;
import java.util.StringJoiner;

class FlakyStatement extends Statement {

    private final Statement base;
    private final Description description;

    private final int iterations;
    private final boolean traceAllFailures;
    private final boolean itemizeSummary;

    private final StringJoiner results = new StringJoiner("\n");
    private Throwable lastException = new UnknownError();

    FlakyStatement(@NonNull final Statement base, @NonNull final Description description, @NonNull final Flaky flaky) {
        this.base = base;
        this.description = description;

        iterations = flaky.iterations();
        traceAllFailures = flaky.traceAllFailures();
        itemizeSummary = flaky.itemizeSummary();
    }

    @SuppressWarnings("unused")
    private FlakyStatement() throws InstantiationException {
        throw new InstantiationException();
    }

    @Override
    public void evaluate() throws Throwable {

        //Run the tests
        final int failureCount = iterate();
        logTestExecutionSummary(failureCount);

        //Only fail if all tests fail
        if (failureCount == iterations) {
            throw lastException;
        }
    }

    private int iterate() {
        int failures = 0;

        for (int i = 0; i < iterations; i++) {
            String attemptCount = (i + 1) + "/" + iterations;
            try {
                base.evaluate();
                handleTestPass(attemptCount);
            } catch (Throwable e) {
                failures++;
                handleTestFailure(attemptCount, e);
            }
        }

        return failures;
    }

    private void logTestExecutionSummary(final int failureCount) {
        final float failureRate = (float) failureCount / iterations;
        final String frLabel = new DecimalFormat("##%").format(failureRate);
        final String summary = String.format("Flaky Test Summary for %s (%s failure rate)",
                description.getDisplayName(),
                frLabel);

        final String newLine = "\n";
        final String emptyLine = newLine + newLine;
        final StringJoiner joiner = new StringJoiner(newLine, emptyLine, emptyLine).add(summary);

        if(itemizeSummary){
            joiner.add(results.toString());
        }

        log(joiner.toString());
    }

    private void handleTestPass(@NonNull final String iteration){
        final String oneLiner = String.format("Flaky test iteration %s passed", iteration);
        log(oneLiner);
        results.add(oneLiner);
    }

    private void handleTestFailure(@NonNull final String iteration, @NonNull final Throwable t) {
        lastException = t;
        final String oneLiner = String.format("Flaky test iteration %s failed", iteration);

        if(traceAllFailures){
            new Throwable(oneLiner, lastException).printStackTrace();
        } else {
            log(oneLiner + ": " + lastException.getMessage());
        }

        if(itemizeSummary) {
            results.add(oneLiner);
        }
    }

    private void log(final String message) {
        System.out.println(message);
    }
}
