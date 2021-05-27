package com.bunchofstring.test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TouchMarkupRule extends LifecycleTestRule {

    private static final Logger LOGGER = Logger.getLogger(TouchMarkupRule.class.getSimpleName());

    @Override
    public void before(){
        if(!showVisualIndicators()){
            LOGGER.log(Level.WARNING, "Problem showing visual indicators");
        }
    }

    @Override
    public void after(){
        if(!hideVisualIndicators()){
            LOGGER.log(Level.WARNING, "Problem hiding visual indicators");
        }
    }

    /**
     * Show cross-hairs for touches and leave a pixel trail
     * @return true if the device received the request to show the indicators, false otherwise
     */
    public static boolean showVisualIndicators(){
        try {
            CoreUtils.executeShellCommand("content insert --uri content://settings/system --bind name:s:pointer_location --bind value:i:1");
            CoreUtils.executeShellCommand("content insert --uri content://settings/system --bind name:s:show_touches --bind value:i:1");
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not show visual indicators", e);
            return false;
        }
    }

    /**
     * Hide cross-hairs for touches as well as the pixel trail
     * @return true if the device received the request to hide the indicators, false otherwise
     */
    public static boolean hideVisualIndicators(){
        try {
            CoreUtils.executeShellCommand("content insert --uri content://settings/system --bind name:s:pointer_location --bind value:i:0");
            CoreUtils.executeShellCommand("content insert --uri content://settings/system --bind name:s:show_touches --bind value:i:0");
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not hide visual indicators", e);
            return false;
        }
    }
}
