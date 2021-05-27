package com.bunchofstring.test;

public final class FreshStartTestRule extends LifecycleTestRule {

    private final String packageName;

    public FreshStartTestRule(final String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void before() throws Throwable {
        CoreUtils.resetApp(packageName);
    }

    @Override
    public void after(){
        //No implementation
    }
}
