package com.bunchofstring.precisiontime.test;

import com.bunchofstring.precisiontime.test.core.TestConfig;
import com.bunchofstring.precisiontime.test.pageobject.MainPageObject;
import com.bunchofstring.test.AppLifecycleTestRule;
import com.bunchofstring.test.ColdStartTestRule;
import com.bunchofstring.test.FrameworkSpeedRule;
import com.bunchofstring.test.TouchMarkupRule;
import com.bunchofstring.test.capture.ClapperboardTestWatcher;
import com.bunchofstring.test.capture.FailureScreenshotTestWatcher;
import com.bunchofstring.test.capture.FailureVideoTestWatcher;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

public class ColdStartTest {

    @ClassRule
    public static RuleChain classRuleChain = RuleChain.emptyRuleChain()
            .around(new FrameworkSpeedRule())
            .around(new TouchMarkupRule());

    @Rule
    public RuleChain testRuleChain = RuleChain.emptyRuleChain()
            .around(new FailureVideoTestWatcher())
            .around(new ClapperboardTestWatcher())
            .around(new ColdStartTestRule(TestConfig.PACKAGE_NAME))
            .around(new AppLifecycleTestRule(TestConfig.PACKAGE_NAME))
            .around(new FailureScreenshotTestWatcher())
            .around(Timeout.seconds(TestConfig.TEST_TIMEOUT_SECONDS));

    @Test
    public void test_GivenNotRunning_WhenLaunch_ThenDoNotAutoFocus() {
        //Then
        Assert.assertFalse("Text field stole focus on launch", MainPageObject.getNtpHostField().isFocused());
    }
}
