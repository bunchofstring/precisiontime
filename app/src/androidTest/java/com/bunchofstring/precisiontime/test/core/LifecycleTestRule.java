package com.bunchofstring.precisiontime.test.core;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class LifecycleTestRule implements TestRule {

    final private Runnable before;
    final private Runnable after;

    public LifecycleTestRule(Runnable before, Runnable after) {
        this.before = before;
        this.after = after;
    }

    @SuppressWarnings("unused")
    private LifecycleTestRule() throws InstantiationException {
        throw new InstantiationException();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try{
                    before.run();
                    base.evaluate();
                }finally{
                    after.run();
                }
            }
        };
    }
}
