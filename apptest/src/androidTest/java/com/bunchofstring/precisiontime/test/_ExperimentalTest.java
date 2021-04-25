package com.bunchofstring.precisiontime.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;

import com.bunchofstring.test.CoreUtils;
import com.bunchofstring.test.FrameworkSpeedRule;
import com.bunchofstring.test.LifecycleTestRule;
import com.bunchofstring.test.NetworkConditioner;
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
import org.junit.rules.TestRule;

import java.io.IOException;
import java.util.Random;

//@Ignore("Only needed to demonstrate failure handling - using different mechanisms for repetition")
public class _ExperimentalTest {

    @ClassRule
    public static TestRule classRule = new FrameworkSpeedRule();

    @Rule
    public RuleChain testRuleChain = RuleChain.emptyRuleChain()
            .around(new FlakyTestRule())
            .around(new FailureVideoTestWatcher())
            .around(new ClapperboardTestWatcher())
            .around(new FailureScreenshotTestWatcher());

    @Flaky(iterations = 10, traceAllFailures = true, itemizeSummary = true)
    @Test
    public void test_NonDeterministic() {
        final int range = 10;
        final int cutoffAfter = 2;
        final int value = new Random().nextInt(range)+1;
        Assert.assertTrue(value + " is beyond the cutoff", value <= cutoffAfter);
    }

    @Flaky
    @Test
    public void test_Failure(){
        Assert.fail("Intentional failure - as an example");
    }

    @Test
    public void test_Pass(){
    }

    @Test
    public void test_Failure2(){
        Assert.fail("Intentional failure2 - as an example");
    }

    //TODO: This style of repetition interacts with the FailureVideoTestWatcher in such a way that crashes the test instrumentation process
    /*@Test
    public void test_Repetition() throws Throwable {
        final Throwable throwableSummary = new Throwable("Failed reflective test execution");
        final Request request = Request.method(CoreUiTest.class, "test_GivenNoFocus_WhenClickServerUrl_ThenGainFocus");
        boolean hasFailures = false;
        for(int i = 0; i < 3; i++) {
            hasFailures = !runTest(request, throwableSummary) || hasFailures;
        }
        if(hasFailures){
            throw throwableSummary;
        }
    }

    private boolean runTest(final Request request, final Throwable summary){
        final Result result = new JUnitCore().run(request);
        result.getFailures().forEach(failure -> summary.addSuppressed(failure.getException()));
        return result.wasSuccessful();
    }*/

}
