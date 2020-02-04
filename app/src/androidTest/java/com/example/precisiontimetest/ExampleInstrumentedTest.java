package com.example.precisiontimetest;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Configurator;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private final static long SHORT_TIMEOUT = 500L;
    private final static long WAIT_FOR_IDLE_TIMEOUT = 1000L;
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
        getNtpHostField().setText(previousNtpServer);
        assertTrue(getDevice().pressEnter());
    }

    @Before
    public void setupTest(){
        //launchApp();
    }

    @Test
    public void test_WhenLaunch_ThenDoNotAutoFocus() {
        //Then
        assertFalse(getNtpHostField().isFocused());
    }

    @Test
    public void test_GivenNoFocus_WhenClickServerUrl_ThenGainFocus() {
        //Given
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
        UiObject2 ntpHostField = getNtpHostField();

        //When
        ntpHostField.setText(NEW_NTP_HOST);
        assertTrue(getDevice().pressEnter());

        //Then
        assertNotNull(getDevice().wait(Until.findObject(By
                .res(getPackageName(),"status_actively_syncing")), 3000));
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
        //return getClass().getPackage().getName();
    }

    private static UiDevice getDevice(){
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        return UiDevice.getInstance(instrumentation);
    }
}
