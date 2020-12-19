package com.bunchofstring.test;

import androidx.annotation.NonNull;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * ClassLifecycleTestRule and its nested class (ClassLifecycleTestRuleHelper) alter the way Junit
 * handles errors and exceptions within class-level setup, like @BeforeClass or @ClassRule.
 *
 * 1. When a problem is detected during class setup, tests inside fail conspicuously (no skip)
 * 2. Stack trace clearly indicates if an error or exception occured during class setup
 */

abstract public class ClassLifecycleTestRule extends LifecycleTestRule {

    private boolean isPreconditionSatisfied;
    private RuntimeException suppressedThrowable;

    /**
     * Sets up the test class.
     * @throws Throwable
     */
    public abstract void before() throws Throwable;

    /**
     * Tears down the test class. This is guaranteed - even if there is a problem during setup or
     * during the test. Will not throw if there was already a problem detected during setup or the
     * test itself.
     * @throws Throwable
     */
    public abstract void after() throws Throwable;

    public static class ClassLifecycleTestRuleHelper extends LifecycleTestRule{

        private final ClassLifecycleTestRule classLifecycleTestRule;

        public ClassLifecycleTestRuleHelper(ClassLifecycleTestRule cltr){
            classLifecycleTestRule = cltr;
        }

        @SuppressWarnings("unused")
        private ClassLifecycleTestRuleHelper() throws InstantiationException {
            throw new InstantiationException();
        }

        @Override @NonNull
        public Statement apply(Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    if(!classLifecycleTestRule.isPreconditionSatisfied) {
                        throw classLifecycleTestRule.suppressedThrowable;
                    }
                    ClassLifecycleTestRuleHelper.super.apply(base, description);
                }
            };
        }

        @Override
        public void before() throws Throwable {

        }

        @Override
        public void after() throws Throwable {

        }
    }

    @Override @NonNull
    public Statement apply(Statement base, Description description) {
        return new LifecycleTestRule() {
            @Override
            public void before() {
                try {
                    ClassLifecycleTestRule.this.before();
                    isPreconditionSatisfied = true;
                } catch (Throwable t){
                    suppressedThrowable = new RuntimeException("Class set up failed", t);
                }
            }

            @Override
            public void after() throws Throwable {
                ClassLifecycleTestRule.this.after();
            }
        }.apply(base, description);
    }

}
