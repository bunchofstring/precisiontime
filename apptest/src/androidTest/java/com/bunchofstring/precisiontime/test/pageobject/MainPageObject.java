package com.bunchofstring.precisiontime.test.pageobject;

import androidx.annotation.NonNull;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.bunchofstring.precisiontime.test.core.TestConfig;
import com.bunchofstring.test.CoreUtils;

import org.junit.Assert;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class MainPageObject {

    private final static String RES_ID_PROGRESS_INDICATOR = "status_actively_syncing";
    private final static String RES_ID_TIME_LABEL = "current_time";
    private final static String RES_NTP_HOST_FIELD = "ntp_host";

    private final static long SHORT_TIMEOUT = TimeUnit.MILLISECONDS.convert(2L, TimeUnit.SECONDS);
    private final static long KEYBOARD_DISMISSAL_DELAY = TimeUnit.MILLISECONDS.toMillis(200L);

    private final static String VALID_NTP_HOST_0 = "0.pool.ntp.org";
    private final static String VALID_NTP_HOST_1 = "1.pool.ntp.org";

    public static void enterNtpHost(@NonNull final String host){
        final UiObject2 ntpHostField = getNtpHostField();
        ntpHostField.click();
        ntpHostField.setText(host);

        //Needs a bit more time to dismiss the keyboard consistently
        final UiDevice device = CoreUtils.getDevice();
        //device.waitForIdle(KEYBOARD_DISMISSAL_DELAY);
        Assert.assertTrue("Could not enter the value", device.pressEnter());
    }

    public static UiObject2 getProgressIndicator(){
        return CoreUtils.getDevice().wait(Until.findObject(By
                .res(TestConfig.PACKAGE_NAME, RES_ID_PROGRESS_INDICATOR)), SHORT_TIMEOUT);
    }

    public static UiObject2 getTimeLabel(final long timeout){
        return CoreUtils.getDevice().wait(Until.findObject(By
                .res(TestConfig.PACKAGE_NAME,RES_ID_TIME_LABEL)
                .text(Pattern.compile(".*\\d.*"))), timeout);
    }

    public static UiObject2 getNtpHostField(){
        return CoreUtils.getDevice().wait(Until.findObject(By
                .res(TestConfig.PACKAGE_NAME, RES_NTP_HOST_FIELD)
                .clickable(true)), SHORT_TIMEOUT);
    }

    public static void changeNtpHost(){
        final String currentHost = getNtpHostField().getText();
        final String nextHost = currentHost.equals(VALID_NTP_HOST_0) ? VALID_NTP_HOST_1 : VALID_NTP_HOST_0;
        enterNtpHost(nextHost);
    }
}
