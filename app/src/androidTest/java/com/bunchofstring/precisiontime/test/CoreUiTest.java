package com.bunchofstring.precisiontime.test;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Configurator;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.bunchofstring.test.flaky.Flaky;
import com.bunchofstring.test.flaky.FlakyTestRule;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CoreUiTest {

    private final static long SHORT_TIMEOUT = 2000L;
    private final static long LONG_TIMEOUT = 1000 * 60 * 3L;
    private final static long WAIT_FOR_IDLE_TIMEOUT = 100L;
    private final static long KEYBOARD_DISMISSAL_TIME = 200L;
    private final static long PREVIOUS_WAIT_FOR_IDLE_TIMEOUT = Configurator.getInstance().getWaitForIdleTimeout();
    private final static String NEW_NTP_HOST = "time.google.com";
    private final static String RES_ID_PROGRESS_INDICATOR = "status_actively_syncing";
    private final static String RES_ID_TIME_LABEL = "current_time";

    private static String previousNtpServer;

    @Rule
    public FlakyTestRule flakyTestRule = new FlakyTestRule();

    @BeforeClass
    public static void setup(){
        Configurator.getInstance().setWaitForIdleTimeout(WAIT_FOR_IDLE_TIMEOUT);
        launchApp();
        previousNtpServer = getNtpHostField().getText();
    }

    @AfterClass
    public static void teardown(){
        launchApp();
        enterNtpHost(previousNtpServer);
        Configurator.getInstance().setWaitForIdleTimeout(PREVIOUS_WAIT_FOR_IDLE_TIMEOUT);
    }

    @Flaky(iterations = 10, traceAllFailures = true, itemizeSummary = true)
    @Test
    public void test_NonDeterministic(){
        final int range = 10;
        final int cutoffAfter = 1;
        final int value = new Random().nextInt(range)+1;
        assertTrue(value+" is beyond the cutoff", value <= cutoffAfter);
    }

    @Category(PerformanceTests.class)
    @Test
    public void test_GivenMemoryPressure_ThenOperateWithinThreshold() {
        //TODO: Set a threshold based on actual, tax the system, and take a measurement
        Log.d("performance (ActivityManager.MemoryInfo)", getActivityManagerMemoryReport());
        Log.d("performance (Debug.MemoryInfo)", getDebugMemoryReport());
    }

    @Test
    public void test_WhenLaunch_ThenDisplayTime() {
        //When
        launchApp();

        //Then
        assertNotNull(getTimeObject());
    }

    @Test
    public void test_GivenNoFocus_WhenClickServerUrl_ThenGainFocus() {
        //Given
        launchApp();
        UiObject2 ntpHostField = getNtpHostField();
        assertFalse("Unexpected focus on NTP host field", ntpHostField.isFocused());

        //When
        ntpHostField.click();

        //Then
        assertTrue("NTP host field not focused", ntpHostField.isFocused());
    }

    @Test
    public void test_GivenNotRunning_WhenLaunch_ThenDoNotAutoFocus() throws IOException {
        //Given
        Runtime.getRuntime().exec(new String[] {"am", "force-stop", getPackageName()});

        //When
        launchApp();

        //Then
        assertFalse("Text field stole focus on launch", getNtpHostField().isFocused());
    }

    @Test
    public void test_GivenTimeDisplayed_WhenChangeServerUrl_ThenInitiateReSync() {
        //Given
        launchApp();
        assertNotNull("Time not displayed", getTimeObject());

        //When
        enterNtpHost(NEW_NTP_HOST);

        //Then
        UiObject2 progressIndicator = getDevice().wait(Until.findObject(By
                .res(getPackageName(), RES_ID_PROGRESS_INDICATOR)), SHORT_TIMEOUT);
        assertNotNull("No indication of re-sync in progress", progressIndicator);
    }

    private UiObject2 getTimeObject(){
        return getDevice().wait(Until.findObject(By
                .res(getPackageName(),RES_ID_TIME_LABEL)
                .text(Pattern.compile(".*\\d.*"))), LONG_TIMEOUT);
    }

    private static void enterNtpHost(@NonNull String host){
        UiObject2 ntpHostField = getNtpHostField();
        ntpHostField.click();
        ntpHostField.setText(host);

        //Needs a bit more time to dismiss the keyboard consistently
        getDevice().waitForIdle(KEYBOARD_DISMISSAL_TIME);
        assertTrue("Could not apply the value", getDevice().pressEnter());
    }

    private static UiObject2 getNtpHostField(){
        return getDevice().wait(Until.findObject(By
                .res(getPackageName(), "ntp_host")
                .clickable(true)), SHORT_TIMEOUT);
    }

    private static void launchApp() {
        final Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(getPackageName());
        context.startActivity(intent);
    }

    private static String getPackageName(){
        return InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
    }

    private static UiDevice getDevice(){
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        return UiDevice.getInstance(instrumentation);
    }

    private String getDebugMemoryReport(){
        Debug.MemoryInfo mi = new Debug.MemoryInfo();
        Debug.getMemoryInfo(mi);
        StringJoiner j = new StringJoiner("\n", " \n", "");
        for(Map.Entry<String, String> map: mi.getMemoryStats().entrySet()){
            j.add(map.getKey() + " = " + map.getValue());
        }
        return j.toString();
    }

    private String getActivityManagerMemoryReport(){
        ActivityManager.MemoryInfo mi2 = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) ApplicationProvider.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        assertNotNull("Could not determine memory usage", activityManager);
        activityManager.getMemoryInfo(mi2);
        return new StringJoiner("\n"," \n","")
                .add("totalMem = " + ((float) mi2.totalMem/1024) + "K")
                .add("availMem = " + ((float) mi2.availMem/1024) + "K")
                .add("threshold = " + ((float) mi2.threshold/1024) + "K  <-- Low Memory Killer")
                .add("lowMemory = " + mi2.lowMemory)
                .toString();
    }
}
