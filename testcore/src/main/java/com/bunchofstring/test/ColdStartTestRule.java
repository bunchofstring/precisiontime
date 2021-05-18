package com.bunchofstring.test;

public final class ColdStartTestRule extends LifecycleTestRule {

    private final String packageName;

    public ColdStartTestRule(final String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void before() throws Throwable {
        CoreUtils.killApp(packageName);
    }

    @Override
    public void after() throws Throwable {
        //No implementation
    }
}
