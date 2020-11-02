package com.bunchofstring.precisiontime.test.pageobject;

import androidx.annotation.NonNull;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.bunchofstring.precisiontime.test.core.TestConfig;
import com.bunchofstring.test.CoreUtils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

public class MainPageObject {

    private final static String RES_ID_PROGRESS_INDICATOR = "status_actively_syncing";
    private final static String RES_ID_TIME_LABEL = "current_time";

    private final static long SHORT_TIMEOUT = TimeUnit.MILLISECONDS.convert(2L, TimeUnit.SECONDS);
    private final static long LONG_TIMEOUT = TimeUnit.MILLISECONDS.convert(2L, TimeUnit.MINUTES);
    private final static long KEYBOARD_DISMISSAL_DELAY = TimeUnit.MILLISECONDS.toMillis(200L);

    public static void enterNtpHost(@NonNull final String host){
        final UiObject2 ntpHostField = getNtpHostField();
        ntpHostField.click();
        ntpHostField.setText(host);

        //Needs a bit more time to dismiss the keyboard consistently
        final UiDevice device = CoreUtils.getDevice();
        device.waitForIdle(KEYBOARD_DISMISSAL_DELAY);
        assertTrue("Could not  the value", device.pressEnter());
    }

    public static UiObject2 getProgressIndicator(){
        return CoreUtils.getDevice().wait(Until.findObject(By
                .res(TestConfig.PACKAGE_NAME, RES_ID_PROGRESS_INDICATOR)), SHORT_TIMEOUT);
    }

    public static UiObject2 getTimeLabel(){
        return CoreUtils.getDevice().wait(Until.findObject(By
                .res(TestConfig.PACKAGE_NAME,RES_ID_TIME_LABEL)
                .text(Pattern.compile(".*\\d.*"))), LONG_TIMEOUT);
    }

    public static UiObject2 getNtpHostField(){
        return CoreUtils.getDevice().wait(Until.findObject(By
                .res(TestConfig.PACKAGE_NAME, "ntp_host")
                .clickable(true)), SHORT_TIMEOUT);
    }
}
