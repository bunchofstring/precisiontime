package com.bunchofstring.precisiontime.test;

import com.bunchofstring.precisiontime.test.core.TestConfig;
import com.bunchofstring.precisiontime.test.pageobject.MainPageObject;
import com.bunchofstring.test.AppLifecycleTestRule;
import com.bunchofstring.test.ColdStartTestRule;
import com.bunchofstring.test.FrameworkSpeedRule;
import com.bunchofstring.test.capture.ClapperboardTestWatcher;
import com.bunchofstring.test.capture.FailureScreenshotTestWatcher;
import com.bunchofstring.test.capture.FailureVideoTestWatcher;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import static org.junit.Assert.assertFalse;

public class ColdStartTest {

    @ClassRule
    public static TestRule classRule = new FrameworkSpeedRule();

    @Rule
    public RuleChain testRuleChain = RuleChain.emptyRuleChain()
            .around(new FailureVideoTestWatcher())
            .around(new ClapperboardTestWatcher())
            .around(new ColdStartTestRule(TestConfig.PACKAGE_NAME))
            .around(new AppLifecycleTestRule(TestConfig.PACKAGE_NAME))
            .around(new FailureScreenshotTestWatcher());

    @Test
    public void test_GivenNotRunning_WhenLaunch_ThenDoNotAutoFocus() {
        //Then
        assertFalse("Text field stole focus on launch", MainPageObject.getNtpHostField().isFocused());
    }
}
