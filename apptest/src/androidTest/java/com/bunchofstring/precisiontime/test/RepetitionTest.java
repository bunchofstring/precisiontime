package com.bunchofstring.precisiontime.test;

import com.bunchofstring.test.FrameworkSpeedRule;
import com.bunchofstring.test.capture.ClapperboardTestWatcher;
import com.bunchofstring.test.capture.FailureScreenshotTestWatcher;
import com.bunchofstring.test.capture.FailureVideoTestWatcher;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

public class RepetitionTest {

    @ClassRule
    public static TestRule classRule = new FrameworkSpeedRule();

    @Rule
    public RuleChain testRuleChain = RuleChain.emptyRuleChain()
            .around(new FailureVideoTestWatcher())
            .around(new ClapperboardTestWatcher())
            .around(new FailureScreenshotTestWatcher());

    @Test
    public void test_Failure(){
//TODO: Uncomment and make sure the video capture still works
/*        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        Assert.fail("Intentional failure - as an example");
    }

    @Test
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
    }
}
