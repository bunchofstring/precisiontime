package com.bunchofstring.precisiontime.test;

import androidx.test.uiautomator.UiObject2;

import com.bunchofstring.precisiontime.test.core.TestConfig;
import com.bunchofstring.precisiontime.test.pageobject.AppPageObject;
import com.bunchofstring.precisiontime.test.pageobject.MainPageObject;
import com.bunchofstring.test.AppLifecycleTestRule;
import com.bunchofstring.test.FrameworkSpeedRule;
import com.bunchofstring.test.LifecycleTestRule;
import com.bunchofstring.test.TouchMarkupRule;
import com.bunchofstring.test.capture.ClapperboardTestWatcher;
import com.bunchofstring.test.capture.FailureScreenshotTestWatcher;
import com.bunchofstring.test.capture.FailureVideoTestWatcher;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CoreUiTest {

    private final static String NEW_NTP_HOST_0 = "0.pool.ntp.org";
    private final static String NEW_NTP_HOST_1 = "1.pool.ntp.org";
    private final static String NEW_NTP_HOST_2 = "2.pool.ntp.org";

    @ClassRule
    public static RuleChain classRuleChain = RuleChain
            .outerRule(new FrameworkSpeedRule())
            .around(new TouchMarkupRule())
            .around(new LifecycleTestRule() {
                @Override
                public void before() throws Throwable {
                    AppPageObject.reset();
                }

                @Override
                public void after() {
                    //No implementation
                }
            });

    @Rule
    public RuleChain testRuleChain = RuleChain.emptyRuleChain()
            .around(new FailureVideoTestWatcher())
            .around(new ClapperboardTestWatcher())
            .around(new AppLifecycleTestRule(TestConfig.PACKAGE_NAME))
            .around(new FailureScreenshotTestWatcher());

    @Test
    public void test_WhenLaunch_ThenDisplayTime() {
        //Then
        assertNotNull("Timed out waiting for network time", MainPageObject.getTimeLabel());
    }

    @Test
    public void test_GivenNoFocus_WhenClickServerUrl_ThenGainFocus() {
        //Given
        AtomicReference<UiObject2> ntpHostField = new AtomicReference<>();
        LifecycleTestRule.establishPrecondition(() -> {
            ntpHostField.set(MainPageObject.getNtpHostField());
            assertFalse("Unexpected focus on NTP host field", ntpHostField.get().isFocused());
        });

        //When
        ntpHostField.get().click();

        //Then
        assertTrue("NTP host field not focused", ntpHostField.get().isFocused());
    }

    @Test
    public void test_GivenTimeDisplayed_WhenChangeServerUrl_ThenInitiateReSync() {
        //Given
        LifecycleTestRule.establishPrecondition(() ->
                assertNotNull("Time not displayed", MainPageObject.getTimeLabel())
        );

        //When
        MainPageObject.enterNtpHost(NEW_NTP_HOST_0);

        //Then
        assertNotNull("No indication of re-sync in progress", MainPageObject.getProgressIndicator());
    }

    @Test
    public void test_GivenTimeSyncInProgress_WhenChangeServerUrl_ThenInitiateReSync() {
        //Given
        LifecycleTestRule.establishPrecondition(() -> {
            MainPageObject.enterNtpHost(NEW_NTP_HOST_1);
            assertNotNull("Time sync is not in progress", MainPageObject.getProgressIndicator());
        });

        //When
        MainPageObject.enterNtpHost(NEW_NTP_HOST_2);

        //Then
        assertNotNull("No indication of re-sync in progress", MainPageObject.getProgressIndicator());
    }
}
