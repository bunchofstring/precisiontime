package com.bunchofstring.precisiontime.test.core;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestUtils {

    private static final Logger LOGGER = Logger.getLogger(TestUtils.class.getSimpleName());
    private static final long SHORT_TIMEOUT = 1000 * 5L;
    private final static long LONG_TIMEOUT = 1000 * 60 * 3L;

    public static String executeShellCommand(String cmd) throws IOException {
        String result = getDevice().executeShellCommand(cmd);
        LOGGER.log(Level.INFO,"Result of '"+cmd+"' is\n"+result);
        return result;
    }

    public static void launchApp() {
        final Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(getPackageName());
        context.startActivity(intent);
        getDevice().wait(Until.findObject(By.pkg(getPackageName())), LONG_TIMEOUT);
    }

    public static boolean showVisualIndicators(){
        try {
            executeShellCommand("content insert --uri content://settings/system --bind name:s:pointer_location --bind value:i:1");
            executeShellCommand("content insert --uri content://settings/system --bind name:s:show_touches --bind value:i:1");
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not hide visual indicators", e);
            return false;
        }
    }

    public static boolean hideVisualIndicators(){
        try {
            executeShellCommand("content insert --uri content://settings/system --bind name:s:pointer_location --bind value:i:0");
            executeShellCommand("content insert --uri content://settings/system --bind name:s:show_touches --bind value:i:0");
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not hide visual indicators", e);
            return false;
        }
    }

    public static void disposeApp() throws IOException {
        Runtime.getRuntime().exec(new String[] {"am", "force-stop", getPackageName()});
        getDevice().wait(Until.gone(By.pkg(getPackageName())), SHORT_TIMEOUT);
        //notworking executeShellCommand("am force-stop "+getPackageName());
    }

    public static String getPackageName(){
        return "com.bunchofstring.precisiontime";
    }

    public static UiDevice getDevice(){
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        return UiDevice.getInstance(instrumentation);
    }
}
