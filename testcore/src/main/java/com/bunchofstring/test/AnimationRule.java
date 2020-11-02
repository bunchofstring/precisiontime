package com.bunchofstring.test;

import java.io.IOException;
import java.util.Arrays;

public class AnimationRule extends LifecycleTestRule {

    private final Iterable<String> ANIMATION_SETTINGS = Arrays.asList(
            "transition_animation_scale",
            "window_animation_scale",
            "animator_duration_scale");

    @Override
    public void before() throws Throwable{
        for (String key : ANIMATION_SETTINGS) {
            setGlobalSetting(key, "0");
        }
    }

    @Override
    public void after() throws Throwable{
        for (String key : ANIMATION_SETTINGS) {
            setGlobalSetting(key, "1");
        }
    }

    private void setGlobalSetting(final String key, final String value) throws IOException {
        final String cmd = String.format("settings put global %s %s", key, value);
        CoreUtils.executeShellCommand(cmd);
    }
}
