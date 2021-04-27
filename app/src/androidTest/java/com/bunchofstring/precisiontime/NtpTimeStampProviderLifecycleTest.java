package com.bunchofstring.precisiontime;

import com.bunchofstring.precisiontime.core.NtpTimestampProvider;
import com.bunchofstring.test.LifecycleTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

public class NtpTimeStampProviderLifecycleTest {

    private NtpTimestampProvider ntp = new NtpTimestampProvider();

    @Rule
    public TestRule rule = RuleChain.emptyRuleChain()
        .around(new LifecycleTestRule() {
            @Override
            public void before() throws Throwable {
                ntp = new NtpTimestampProvider();
            }

            @Override
            public void after() throws Throwable {
                ntp = null;
            }
        });

    @Test
    public void test_WhenStart_AttemptSync() {
        ntp.start();
        Assert.assertTrue("Start did not initiate network time sync", ntp.isSyncInProgress());
    }

    @Test
    public void test_CanStopPeriodicSync() {
        ntp.start();
        ntp.stop();
        Assert.assertFalse("Could not stop periodic network time synchronization", ntp.isSyncInProgress());
    }
}
