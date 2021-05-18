package com.bunchofstring.precisiontime.test;

import com.bunchofstring.precisiontime.test.core.TestConfig;
import com.bunchofstring.test.CoreUtils;
import com.bunchofstring.test.FrameworkSpeedRule;
import com.bunchofstring.test.LifecycleTestRule;
import com.bunchofstring.test.TouchMarkupRule;
import com.bunchofstring.test.capture.ClapperboardTestWatcher;
import com.bunchofstring.test.capture.FailureScreenshotTestWatcher;
import com.bunchofstring.test.capture.FailureVideoTestWatcher;
import com.bunchofstring.test.flaky.FlakyTestRule;
import com.bunchofstring.test.netcon.NetworkConditioner;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import java.io.IOException;

public final class ConnectivityTest {

    private static final String PING_TARGET = "8.8.8.8"; //Google DNS
    private NetworkConditioner networkConditioner;

    @ClassRule
    public static RuleChain classRuleChain = RuleChain.emptyRuleChain()
            .around(new FrameworkSpeedRule())
            .around(new TouchMarkupRule());

    @Rule
    public RuleChain testRuleChain = RuleChain.emptyRuleChain()
            .around(new FlakyTestRule())
            .around(new FailureVideoTestWatcher())
            .around(new ClapperboardTestWatcher())
            .around(new FailureScreenshotTestWatcher())
            .around(Timeout.seconds(TestConfig.TEST_TIMEOUT_SECONDS))
            .around(new LifecycleTestRule() {
                @Override
                public void before() throws Throwable {
                    networkConditioner = new NetworkConditioner();
                }

                @Override
                public void after() throws Throwable {
                    networkConditioner.reset();
                    networkConditioner = null;
                }
            });

    //@Flaky(iterations = 10, traceAllFailures = true, itemizeSummary = true)
    @Test
    public void testConnectionRecovery() throws Exception {
        //Arrange
        LifecycleTestRule.establishPrecondition(() -> {
            blockInternetConnectivity();
            Assert.assertFalse("Internet connection is (unexpectedly) still active", isPingSuccessful());
        });

        //Act
        establishInternetConnectivity();

        //Assert
        Assert.assertTrue("Internet connection did not recover", isPingSuccessful());
    }

    private void establishInternetConnectivity() throws Exception {
        networkConditioner.setWifiEnabled(true);
        connectWifiNetwork();
    }

    private void blockInternetConnectivity() throws Exception{
        networkConditioner.setDataEnabled(false);
        networkConditioner.setWifiEnabled(false);
    }

    private void connectWifiNetwork() throws Exception {
        if(CoreUtils.isEmulator()) {
            networkConditioner.pairConnectWifiNetwork("AndroidWifi");
        }else {
            networkConditioner.pairConnectWifiNetwork("test", "test");
        }
    }

    private static boolean isPingSuccessful() throws IOException {
        final String result = CoreUtils.executeShellCommand("ping -c 1 "+PING_TARGET);
        return result.contains("bytes from");
    }
}
