package com.example.precisiontimetest;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Configurator;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private final static long SHORT_TIMEOUT = 2000L;
    private final static long WAIT_FOR_IDLE_TIMEOUT = 500L;
    private final static long PREVIOUS_WAIT_FOR_IDLE_TIMEOUT = Configurator.getInstance().getWaitForIdleTimeout();
    private final static String NEW_NTP_HOST = "nist.time.gov";


    private static String previousNtpServer;

    @BeforeClass
    public static void setup(){
        Configurator.getInstance().setWaitForIdleTimeout(WAIT_FOR_IDLE_TIMEOUT);
        launchApp();
        previousNtpServer = getNtpHostField().getText();
    }

    @AfterClass
    public static void teardown(){
        Configurator.getInstance().setWaitForIdleTimeout(PREVIOUS_WAIT_FOR_IDLE_TIMEOUT);
        launchApp();
        //enterNtpHost(previousNtpServer);
    }

    @Test
    public void test_GivenNotRunning_WhenLaunch_ThenDoNotAutoFocus() throws IOException {
        //Given
        Runtime.getRuntime().exec(new String[] {"am", "force-stop", getPackageName()});

        //When
        launchApp();

        //Then
        assertFalse(getNtpHostField().isFocused());
    }

    @Test
    public void test_GivenNoFocus_WhenClickServerUrl_ThenGainFocus() {
        //Given
        launchApp();
        UiObject2 ntpHostField = getNtpHostField();
        assertFalse(ntpHostField.isFocused());

        //When
        ntpHostField.click();

        //Then
        assertTrue(ntpHostField.isFocused());
    }

    @Test
    public void test_WhenChangeServerUrl_ThenUpdateLabel() {
        //Given
        launchApp();

        //When
        enterNtpHost(NEW_NTP_HOST);

        //Then
        assertNotNull(getDevice().wait(Until.findObject(By
                .res(getPackageName(),"status_actively_syncing")), SHORT_TIMEOUT));
    }

    private static void enterNtpHost(@NonNull String host){
        UiObject2 ntpHostField = getNtpHostField();
        ntpHostField.click();
        ntpHostField.setText(host);

        assertTrue(getDevice().pressEnter());
        getDevice().waitForIdle(5000L);
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
}
