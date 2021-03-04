package com.bunchofstring.test;

public class FreshStartTestRule extends LifecycleTestRule {

    private final String packageName;

    public FreshStartTestRule(final String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void before() throws Throwable {
        CoreUtils.resetApp(packageName);
    }

    @Override
    public void after() throws Throwable {
        //No implementation
    }
}
