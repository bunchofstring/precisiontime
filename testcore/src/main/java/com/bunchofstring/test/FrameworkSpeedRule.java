package com.bunchofstring.test;

import androidx.test.uiautomator.Configurator;

public class FrameworkSpeedRule extends LifecycleTestRule {

    private final static long WAIT_FOR_IDLE_TIMEOUT = 100L;
    private final static long PREVIOUS_WAIT_FOR_IDLE_TIMEOUT = Configurator.getInstance().getWaitForIdleTimeout();

    @Override
    public void before() {
        Configurator.getInstance().setWaitForIdleTimeout(WAIT_FOR_IDLE_TIMEOUT);
    }

    @Override
    public void after() {
        Configurator.getInstance().setWaitForIdleTimeout(PREVIOUS_WAIT_FOR_IDLE_TIMEOUT);
    }
}
