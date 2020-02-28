package com.example.precisiontimetest;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A tool to help manage "flaky" automated tests - which produce inconsistent results based on
 * factors outside of the test's control. The primary purpose is to configure certain tests to
 * execute repeatedly, in order to make a more reliable determination about pass vs. fail.
 * <p>
 * This is done by executing the flaky test N times (where N > 1). The outcome is failure only if
 * N/N attempts result in failure. In all other cases, such a test would pass. Some examples to
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
 * feedback. After that, prioritize efforts to identify and eliminate the source of flakiness
 * 2. There may be some temptation to use this liberally throughout a collection of tests. That
 * would be a misuse of the tool and a bad process smell - which leads to wasted resources and
 * increased total cost of ownership (among other undesirable side-effects)
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
        Flaky flaky = description.getAnnotation(Flaky.class);
        if (flaky == null) {
            return base;
        }else{
            return new FlakyStatement(base, description, flaky);
        }
    }
}