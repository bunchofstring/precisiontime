package com.bunchofstring.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class LifecycleTestRule implements TestRule {

    private static final Logger LOGGER = Logger.getLogger(LifecycleTestRule.class.getSimpleName());

    private boolean isPreconditionSatisfied;

    public abstract void before() throws Throwable;

    public abstract void after() throws Throwable;

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    attemptBefore();
                    base.evaluate();
                } finally {
                    attemptAfter();
                }
            }
        };
    }

    public interface Precondition {
        void given() throws Throwable;
    }

    public static void establishPrecondition(final Precondition p){
        given(p);
    }

    private void attemptBefore() {
        given(() -> {
            LifecycleTestRule.this.before();
            isPreconditionSatisfied = true;
        });
    }

    /**
     * Activates the provided precondition. Ensures that any exception stacktrace indicates a
     * problem while arranging the test - not in the body of the test itself.
     * @param p Precondition to be activated
     */
    private static void given(final Precondition p){
        try {
            p.given();
        } catch (Throwable t) {
            throw new RuntimeException("Problem setting up (i.e. precondition not satisfied)", t);
        }
    }

    private void attemptAfter() {
        try {
            LifecycleTestRule.this.after();
        } catch (Throwable t) {
            final RuntimeException rte = new RuntimeException("Problem cleaning up (i.e. postcondition not satisfied)", t);
            if(isPreconditionSatisfied) {
                throw rte;
            }else{
                LOGGER.log(Level.SEVERE, "Exception was downgraded to a severe log because the setup failure occurred first", rte);
            }
        }
    }
}
