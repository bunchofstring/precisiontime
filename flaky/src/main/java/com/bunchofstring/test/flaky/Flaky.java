package com.bunchofstring.test.flaky;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
public @interface Flaky {
    /**
     * Optionally, specify the number of <code>iterations</code> required to determine the test
     * result. Must be in the range of 2 to Integer.MAX_VALUE, inclusive. Default is 3.
     */
    int iterations() default 3;

    /**
     * Optionally, specify true to log all stack traces.
     */
    boolean traceAllFailures() default false;

    /**
     * Optionally, specify true to log a list of brief descriptions after all iterations are done.
     */
    boolean itemizeSummary() default false;
}
