package com.bunchofstring.test;

import java.io.IOException;

//TODO: Normally would be final. Only opened it so the test can launch *with arguments* for CoreUiTest
public class AppLifecycleTestRule extends LifecycleTestRule {

    private final String packageName;

    public AppLifecycleTestRule(final String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void before() {
        CoreUtils.launchApp(packageName);
    }

    @Override
    public final void after() throws IOException {
        CoreUtils.killApp(packageName);
    }
}
