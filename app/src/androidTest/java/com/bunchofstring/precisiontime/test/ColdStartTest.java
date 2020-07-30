package com.bunchofstring.precisiontime.test;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Configurator;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.bunchofstring.precisiontime.test.core.TestUtils;
import com.bunchofstring.test.capture.CaptureTestWatcher;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertFalse;

public class ColdStartTest {

    private static final Logger LOGGER = Logger.getLogger(ColdStartTest.class.getSimpleName());

    private final static long SHORT_TIMEOUT = 2000L;
    private final static long WAIT_FOR_IDLE_TIMEOUT = 100L;
    private final static long PREVIOUS_WAIT_FOR_IDLE_TIMEOUT = Configurator.getInstance().getWaitForIdleTimeout();

    private CaptureTestWatcher captureTestWatcher = new CaptureTestWatcher(InstrumentationRegistry.getInstrumentation());
    private ExternalResource lifecycleRule = new ExternalResource(){
        @Override
        protected void before() throws Throwable {
            Configurator.getInstance().setWaitForIdleTimeout(WAIT_FOR_IDLE_TIMEOUT);
            TestUtils.disposeApp();
            TestUtils.launchApp();
        }

        @Override
        protected void after() {
            System.out.println("talldave - rule-based afterclass");
            try {
                LOGGER.log(Level.INFO,"foofoo8");
                TestUtils.disposeApp();
                LOGGER.log(Level.INFO,"foofoo9");
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Problem cleaning up", e);
            } finally {
                LOGGER.log(Level.INFO,"foofoo10");
                Configurator.getInstance().setWaitForIdleTimeout(PREVIOUS_WAIT_FOR_IDLE_TIMEOUT);
                LOGGER.log(Level.INFO,"foofoo11");
            }
        }
    };

    @Rule
    public RuleChain ruleChain = RuleChain
            .outerRule(lifecycleRule)
            .around(captureTestWatcher);

    @Test
    public void test_GivenNotRunning_WhenLaunch_ThenDoNotAutoFocus() throws IOException {
        //Given
        TestUtils.disposeApp();

        //When
        TestUtils.launchApp();

        //Then
        assertFalse("Text field stole focus on launch", getNtpHostField().isFocused());
    }


    private static UiObject2 getNtpHostField(){
        return TestUtils.getDevice().wait(Until.findObject(By
                .res(TestUtils.getPackageName(), "ntp_host")
                .clickable(true)), SHORT_TIMEOUT);
    }
}
