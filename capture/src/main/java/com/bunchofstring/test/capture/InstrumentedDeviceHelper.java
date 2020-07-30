package com.bunchofstring.test.capture;

import android.app.Instrumentation;
import android.content.Context;

import androidx.test.uiautomator.UiDevice;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class InstrumentedDeviceHelper {

    private static final Logger LOGGER = Logger.getLogger(InstrumentedDeviceHelper.class.getSimpleName());

    private final Instrumentation instrumentation;

    InstrumentedDeviceHelper(Instrumentation i) {
        instrumentation = i;
    }

    Context getApplicationContext(){
        return instrumentation.getTargetContext().getApplicationContext();
    }

    UiDevice getDevice(){
        return UiDevice.getInstance(instrumentation);
    }

    boolean takeScreenshot(File f){
        return getDevice().takeScreenshot(f);
    }

    String executeShellCommand(String cmd) throws IOException {
        String result = getDevice().executeShellCommand(cmd);
        LOGGER.log(Level.INFO, "Result of '"+cmd+"' is\n"+result);
        return result;
    }
}
