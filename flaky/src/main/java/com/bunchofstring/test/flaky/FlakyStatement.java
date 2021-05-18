package com.bunchofstring.test.flaky;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

final class FlakyStatement extends Statement {

    private static final Logger LOGGER = Logger.getLogger(FlakyStatement.class.getSimpleName());

    private final Statement base;
    private final Description description;

    private final int iterations;
    private final boolean traceAllFailures;
    private final boolean itemizeSummary;

    private final StringJoiner results = new StringJoiner("\n");

    private final Collection<Throwable> supressedThrowables = new ArrayList<>();

    FlakyStatement(final Statement base, final Description description, final Flaky flaky) {
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
        final int failureCount = executeIterations();
        logTestExecutionSummary(failureCount);

        //Only fail if all tests fail
        if (failureCount == iterations) {
            final FlakyException summaryException = new FlakyException(String.format(Locale.getDefault(),
                    "Failed %d/%d attempts (traceAllFailures=%b, itemizeSummary=%b)",
                    failureCount, iterations, traceAllFailures, itemizeSummary));
            supressedThrowables.forEach(summaryException::addSuppressed);
            throw summaryException;
        }
    }

    private int executeIterations() {
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
                description.getDisplayName(), frLabel);

        final String newLine = "\n";
        final String emptyLine = newLine + newLine;
        final StringJoiner joiner = new StringJoiner(newLine, emptyLine, emptyLine).add(summary);

        if(itemizeSummary){
            joiner.add(results.toString());
        }

        log(joiner.toString());
    }

    private void handleTestPass(final String iteration){
        final String oneLiner = String.format("Flaky test iteration %s passed", iteration);
        log(oneLiner);

        if(itemizeSummary) {
            results.add(oneLiner);
        }
    }

    private void handleTestFailure(final String iteration, final Throwable t) {
        supressedThrowables.add(t);
        final String oneLiner = String.format("Flaky test iteration %s failed", iteration);

        if(traceAllFailures){
            LOGGER.log(Level.SEVERE,oneLiner,t);
        } else {
            log(oneLiner + ": " + t.getMessage());
        }

        if(itemizeSummary) {
            results.add(oneLiner);
        }
    }

    private void log(final String message) {
        LOGGER.log(Level.INFO,message);
    }
}
