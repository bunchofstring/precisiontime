package com.bunchofstring.precisiontime.test;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.bunchofstring.precisiontime.test.core.TestConfig;
import com.bunchofstring.precisiontime.test.pageobject.AppPageObject;
import com.bunchofstring.precisiontime.test.pageobject.MainPageObject;
import com.bunchofstring.test.AppLifecycleTestRule;
import com.bunchofstring.test.CoreUtils;
import com.bunchofstring.test.FrameworkSpeedRule;
import com.bunchofstring.test.FreshStartTestRule;
import com.bunchofstring.test.LifecycleTestRule;
import com.bunchofstring.test.TouchMarkupRule;
import com.bunchofstring.test.capture.ClapperboardTestWatcher;
import com.bunchofstring.test.capture.FailureScreenshotTestWatcher;
import com.bunchofstring.test.capture.FailureVideoTestWatcher;
import com.bunchofstring.test.flaky.Flaky;
import com.bunchofstring.test.flaky.FlakyTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class CoreUiTest {

    private final static long NTP_FETCH_TIMEOUT = TimeUnit.MILLISECONDS.convert(2L, TimeUnit.MINUTES);
    private final static String MOCK_NTP_HOST = "10.0.2.2";

    @ClassRule
    public static RuleChain classRuleChain = RuleChain.emptyRuleChain()
            .around(new FrameworkSpeedRule())
            .around(new TouchMarkupRule())
            .around(new FreshStartTestRule(TestConfig.PACKAGE_NAME));

    @Rule
    public RuleChain testRuleChain = RuleChain.emptyRuleChain()
            .around(new FlakyTestRule())
            .around(new FailureVideoTestWatcher())
            .around(new ClapperboardTestWatcher())
            .around(new AppLifecycleTestRule(TestConfig.PACKAGE_NAME) {
                @Override
                public void before() {
                    if(TestConfig.USE_MOCK_NTP_HOST) {
                        AppPageObject.launchWithHost(MOCK_NTP_HOST);
                        CoreUtils.getDevice().wait(Until.findObject(By.pkg(TestConfig.PACKAGE_NAME)), 2000L);
                    }else{
                        super.before();
                    }
                }
            })
            .around(new FailureScreenshotTestWatcher())
            .around(Timeout.seconds(TestConfig.TEST_TIMEOUT_SECONDS));

    //In a 50 iteration trial, failure rate was ~24%. Cause was always "timed out waiting for network time"
    //4 iterations (opportunities to pass) per result reduces this to a ~0.33% failure rate due to flakiness
    @Flaky(iterations = TestConfig.USE_MOCK_NTP_HOST ? 1 : 4, traceAllFailures = true, itemizeSummary = true)
    @Test
    public void test_WhenLaunch_ThenDisplayTime() throws IOException {
        //Assert
        Assert.assertNotNull("Timed out waiting for network time", MainPageObject.getTimeLabel(NTP_FETCH_TIMEOUT));
    }

    //In a 50 iteration trial, failure rate was ~42%. Cause was always a "problem setting up" due to "time not displayed"
    //6 iterations per result reduces this to a ~0.55% failure rate
    //TODO: Leverage a test double for the NTP server
    @Flaky(iterations = TestConfig.USE_MOCK_NTP_HOST ? 2 : 6 , traceAllFailures = true, itemizeSummary = true)
    @Test
    public void test_GivenTimeDisplayed_WhenChangeServerUrl_ThenInitiateReSync() {
        //Arrange
        LifecycleTestRule.establishPrecondition(() -> {
            final UiObject2 timeLabel = MainPageObject.getTimeLabel(NTP_FETCH_TIMEOUT);
            Assert.assertNotNull("Time not displayed", timeLabel);
        });

        //Act
        MainPageObject.changeNtpHost();

        //Assert
        Assert.assertNotNull("No indication of re-sync in progress", MainPageObject.getProgressIndicator());
    }

    //In a 100 iteration trial, failure rate was ~2%. Cause was always a "problem setting up" due to "time sync is not in progress"
    //2 iterations per result reduces this to a ~0.04% failure rate
    @Flaky(iterations = 2, traceAllFailures = true, itemizeSummary = true)
    @Test
    public void test_GivenTimeSyncInProgress_WhenChangeServerUrl_ThenInitiateReSync() {
        //Arrange
        LifecycleTestRule.establishPrecondition(() -> {
            MainPageObject.changeNtpHost();
            Assert.assertNotNull("Time sync is not in progress", MainPageObject.getProgressIndicator());
        });

        //Act
        MainPageObject.changeNtpHost();

        //Assert
        Assert.assertNotNull("No indication of re-sync in progress", MainPageObject.getProgressIndicator());
    }

    @Test
    public void test_GivenNoFocus_WhenClickServerUrl_ThenGainFocus() {
        //Arrange
        final UiObject2 ntpHostField = MainPageObject.getNtpHostField();
        LifecycleTestRule.establishPrecondition(() -> {
            Assert.assertNotNull("Could not find the NTP host field", ntpHostField);
            Assert.assertFalse("Unexpected focus on NTP host field", ntpHostField.isFocused());
        });

        //Act
        ntpHostField.click();

        //Assert
        Assert.assertTrue("NTP host field not focused", ntpHostField.isFocused());
    }
}
