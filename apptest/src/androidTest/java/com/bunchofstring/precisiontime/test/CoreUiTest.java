package com.bunchofstring.precisiontime.test;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.uiautomator.UiObject2;

import com.bunchofstring.precisiontime.test.core.TestConfig;
import com.bunchofstring.precisiontime.test.pageobject.MainPageObject;
import com.bunchofstring.test.AppLifecycleTestRule;
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

import java.util.concurrent.TimeUnit;

public final class CoreUiTest {

    private final static long NTP_FETCH_TIMEOUT = TimeUnit.MILLISECONDS.convert(2L, TimeUnit.MINUTES);

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
            .around(new AppLifecycleTestRule(TestConfig.PACKAGE_NAME))
            .around(new FailureScreenshotTestWatcher())
            .around(Timeout.seconds(TestConfig.TEST_TIMEOUT_SECONDS));

    //In a 50 iteration trial, failure rate was ~24%. Cause was always "timed out waiting for network time"
    //3 iterations (opportunities to pass) reduces this to a ~1.4% failure rate
    @Flaky(iterations = 3, traceAllFailures = true, itemizeSummary = true)
    @Test
    public void test_WhenLaunch_ThenDisplayTime() {
        //Then
        Assert.assertNotNull("Timed out waiting for network time", MainPageObject.getTimeLabel(NTP_FETCH_TIMEOUT));
    }

    //In a 50 iteration trial, failure rate was ~42%. Cause was always a "problem setting up" due to "time not displayed"
    //5 iterations reduces this to a ~1.3% failure rate
    //TODO: Leverage a test double for the NTP server
    @Flaky(iterations = 5, traceAllFailures = true, itemizeSummary = true)
    @Test
    public void test_GivenTimeDisplayed_WhenChangeServerUrl_ThenInitiateReSync() {
        //Given
        LifecycleTestRule.establishPrecondition(() -> {
            final UiObject2 timeLabel = MainPageObject.getTimeLabel(NTP_FETCH_TIMEOUT);
            Assert.assertNotNull("Time not displayed", timeLabel);
        });

        //When
        MainPageObject.changeNtpHost();

        //Then
        Assert.assertNotNull("No indication of re-sync in progress", MainPageObject.getProgressIndicator());
    }

    @Test
    public void test_GivenTimeSyncInProgress_WhenChangeServerUrl_ThenInitiateReSync() {
        //Given
        LifecycleTestRule.establishPrecondition(() -> {
            MainPageObject.changeNtpHost();
            Assert.assertNotNull("Time sync is not in progress", MainPageObject.getProgressIndicator());
        });

        //When
        MainPageObject.changeNtpHost();

        //Then
        Assert.assertNotNull("No indication of re-sync in progress", MainPageObject.getProgressIndicator());
    }

    @Test
    public void test_GivenNoFocus_WhenClickServerUrl_ThenGainFocus() {
        //Given
        final UiObject2 ntpHostField = MainPageObject.getNtpHostField();
        LifecycleTestRule.establishPrecondition(() -> {
            Assert.assertNotNull("Could not find the NTP host field", ntpHostField);
            Assert.assertFalse("Unexpected focus on NTP host field", ntpHostField.isFocused());
        });

        //When
        ntpHostField.click();

        //Then
        Assert.assertTrue("NTP host field not focused", ntpHostField.isFocused());
    }
}
