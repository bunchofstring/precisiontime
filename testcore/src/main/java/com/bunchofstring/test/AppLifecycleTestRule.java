package com.bunchofstring.test;

import java.io.IOException;

public final class AppLifecycleTestRule extends LifecycleTestRule {

    private final String packageName;

    public AppLifecycleTestRule(final String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void before() {
        CoreUtils.launchApp(packageName);
    }

    @Override
    public void after() throws IOException {
        CoreUtils.killApp(packageName);
    }
}
