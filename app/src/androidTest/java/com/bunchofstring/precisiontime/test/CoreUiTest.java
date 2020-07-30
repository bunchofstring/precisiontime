package com.bunchofstring.precisiontime.test;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Configurator;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.bunchofstring.precisiontime.test.core.TestUtils;
import com.bunchofstring.test.capture.CaptureTestWatcher;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CoreUiTest {

    private static final Logger LOGGER = Logger.getLogger(CoreUiTest.class.getSimpleName());

    private final static long SHORT_TIMEOUT = TimeUnit.MILLISECONDS.convert(2L, TimeUnit.SECONDS);
    private final static long LONG_TIMEOUT = TimeUnit.MILLISECONDS.convert(3L, TimeUnit.MINUTES);

    private final static long WAIT_FOR_IDLE_TIMEOUT = 100L;
    private final static long KEYBOARD_DISMISSAL_TIME = 200L;
    private final static long PREVIOUS_WAIT_FOR_IDLE_TIMEOUT = Configurator.getInstance().getWaitForIdleTimeout();
    private final static String NEW_NTP_HOST = "0.pool.ntp.org";
    private final static String RES_ID_PROGRESS_INDICATOR = "status_actively_syncing";
    private final static String RES_ID_TIME_LABEL = "current_time";

    private static String previousNtpServer;

    @ClassRule
    public static ExternalResource testClassLifecycleRule = new ExternalResource(){
        @Override
        protected void before() throws IOException {
            System.out.println("talldave - rule-based beforeclass");
            Configurator.getInstance().setWaitForIdleTimeout(WAIT_FOR_IDLE_TIMEOUT);
            TestUtils.showVisualIndicators();
        }

        @Override
        protected void after() {
            System.out.println("talldave - rule-based afterclass");
            Configurator.getInstance().setWaitForIdleTimeout(PREVIOUS_WAIT_FOR_IDLE_TIMEOUT);
            TestUtils.hideVisualIndicators();
        }
    };

    private TestWatcher clapperboardWatcher = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            toast(description.getMethodName() + "\n\n started");
        }

        @Override
        protected void finished(Description description) {
            toast(description.getMethodName() + "\n\n finished");
        }
    };
    private CaptureTestWatcher captureTestWatcher = new CaptureTestWatcher(InstrumentationRegistry.getInstrumentation());
    private ExternalResource testLifecycleRule = new ExternalResource(){
        @Override
        protected void before() {
            System.out.println("talldave - rule-based before");
            TestUtils.launchApp();
            previousNtpServer = getNtpHostField().getText();
        }

        @Override
        protected void after() {
            System.out.println("talldave - rule-based after");
            try {
                enterNtpHost(previousNtpServer);
                TestUtils.disposeApp();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Problem while cleaning up", e);
            }
        }
    };

    @Rule
    public RuleChain ruleChain = RuleChain
            .outerRule(testLifecycleRule)
            .around(captureTestWatcher);
            //.around(clapperboardWatcher);

    @Test
    public void testFailureFoo(){
        fail();
    }

    @Test
    public void test_WhenLaunch_ThenDisplayTime() {
        //Then
        assertNotNull(getTimeLabel());
    }

    @Test
    public void test_GivenNoFocus_WhenClickServerUrl_ThenGainFocus() {
        //Given
        UiObject2 ntpHostField = getNtpHostField();
        assertFalse("Unexpected focus on NTP host field", ntpHostField.isFocused());

        //When
        ntpHostField.click();

        //Then
        assertTrue("NTP host field not focused", ntpHostField.isFocused());
    }

    @Test
    public void test_GivenTimeDisplayed_WhenChangeServerUrl_ThenInitiateReSync() {
        //Given
        assertNotNull("Time not displayed", getTimeLabel());

        //When
        enterNtpHost(NEW_NTP_HOST);

        //Then
        assertNotNull("No indication of re-sync in progress", getProgressIndicator());
    }

    @Test
    public void test_GivenTimeSyncInProgress_WhenChangeServerUrl_ThenInitiateReSync() {
        //Given
        assertNotNull("Time sync is not in progress", getProgressIndicator());

        //When
        enterNtpHost(NEW_NTP_HOST);

        //Then
        UiObject2 progressIndicator = TestUtils.getDevice().wait(Until.findObject(By
                .res(TestUtils.getPackageName(), RES_ID_PROGRESS_INDICATOR)), SHORT_TIMEOUT);
        assertNotNull("No indication of re-sync in progress", progressIndicator);
    }

    private static void enterNtpHost(@NonNull String host){
        UiObject2 ntpHostField = getNtpHostField();
        ntpHostField.click();
        ntpHostField.setText(host);

        //Needs a bit more time to dismiss the keyboard consistently
        UiDevice device = TestUtils.getDevice();
        device.waitForIdle(KEYBOARD_DISMISSAL_TIME);
        assertTrue("Could not apply the value", device.pressEnter());
    }

    private void toast(final String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Context context =  InstrumentationRegistry.getInstrumentation().getTargetContext();
            Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
        });
    }

    private UiObject2 getProgressIndicator(){
        return TestUtils.getDevice().wait(Until.findObject(By
                .res(TestUtils.getPackageName(), RES_ID_PROGRESS_INDICATOR)), SHORT_TIMEOUT);
    }

    private UiObject2 getTimeLabel(){
        return TestUtils.getDevice().wait(Until.findObject(By
                .res(TestUtils.getPackageName(),RES_ID_TIME_LABEL)
                .text(Pattern.compile(".*\\d.*"))), LONG_TIMEOUT);
    }

    private static UiObject2 getNtpHostField(){
        return TestUtils.getDevice().wait(Until.findObject(By
                .res(TestUtils.getPackageName(), "ntp_host")
                .clickable(true)), SHORT_TIMEOUT);
    }
}
