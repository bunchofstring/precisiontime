package com.bunchofstring.test;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CoreUtils {

    private static final Logger LOGGER = Logger.getLogger(CoreUtils.class.getSimpleName());
    private static final long SHORT_TIMEOUT = 1000 * 5L;
    private final static long LONG_TIMEOUT = 1000 * 60 * 3L;

    public static String executeShellCommand(final String cmd) throws IOException {
        final String trimmedCmd = cmd.trim();
        final String result = getDevice().executeShellCommand(trimmedCmd);
        LOGGER.log(Level.INFO,"Result of '"+trimmedCmd+"' is\n"+result);
        return result;
    }

    public static void grantPermission(final String packageName, final String... permission) throws IOException {
        for(final String p : permission) {
            executeShellCommand("pm grant " + packageName + " " + p);
        }
    }

    public static void launchApp(final String packageName) {
        final Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        context.startActivity(intent);
        getDevice().wait(Until.findObject(By.pkg(packageName)), LONG_TIMEOUT);
    }

    private static Intent getLaunchIntent(final Context context, final String packageName) {
        Intent intent = null;
        //for(int i = 0; i < 10; i++){
        intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
//            if(Objects.nonNull(intent)) break;
//            try {
//                Thread.sleep((long) Math.pow(i,3));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        return intent;
    }

    public static void killApp(final String packageName) throws IOException {
        executeShellCommand("am force-stop " + packageName);
        getDevice().wait(Until.gone(By.pkg(packageName)), SHORT_TIMEOUT);
    }

    public static void resetApp(final String packageName) throws IOException {
        executeShellCommand("pm clear " + packageName);
        getDevice().wait(Until.gone(By.pkg(packageName)), SHORT_TIMEOUT);
    }

    public static UiDevice getDevice(){
        final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        return UiDevice.getInstance(instrumentation);
    }

    public static UiDevice getDevice(Instrumentation i){
        return UiDevice.getInstance(i);
    }

    public static Context getTargetApplicationContext(Instrumentation i){
        return i.getTargetContext().getApplicationContext();
    }

    public static boolean isEmulator(){
        final boolean returnValue = Build.PRODUCT.startsWith("sdk");
        LOGGER.log(Level.INFO,"isEmulator() "+returnValue+" "+Build.PRODUCT);
        return returnValue;
    }
}
