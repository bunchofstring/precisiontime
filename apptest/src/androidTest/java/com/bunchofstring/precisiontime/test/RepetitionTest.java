package com.bunchofstring.precisiontime.test;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Until;

import com.bunchofstring.test.CoreUtils;
import com.bunchofstring.test.FrameworkSpeedRule;
import com.bunchofstring.test.LifecycleTestRule;
import com.bunchofstring.test.capture.Capture;
import com.bunchofstring.test.capture.CaptureException;
import com.bunchofstring.test.capture.ClapperboardTestWatcher;
import com.bunchofstring.test.capture.FailureScreenshotTestWatcher;
import com.bunchofstring.test.capture.FailureVideoTestWatcher;
import com.bunchofstring.test.capture.RecordingInProgress;
import com.bunchofstring.test.flaky.Flaky;
import com.bunchofstring.test.flaky.FlakyTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RepetitionTest {

    private static final Logger LOGGER = Logger.getLogger(RepetitionTest.class.getSimpleName());

    @ClassRule
    public static TestRule classRule = new FrameworkSpeedRule();

LifecycleTestRule l = new LifecycleTestRule() {

    RecordingInProgress rip;

    @Override
    public void before() throws Throwable {
        //doIt("foo"); //Works!
        rip = Capture.newVideoRecording("foo");
        //NTS: It does not seem to matter if the delay is in before or after. Beyond a certain delay it breaks :(
    }

    @Override
    public void after() throws Throwable {

        try {
            Thread.sleep(2000); //1500 works intermittently. 1000 works more often
        } catch (InterruptedException ignore){}

        //CoreUtils.getDevice().wait(Until.findObject(By.text("coolio")),2000);

        //rip.finish();

        //NTS: No amount of waiting seems to help

        //doIt("bar"); //Works!
    }
};
    @Rule
    public RuleChain testRuleChain = RuleChain.emptyRuleChain()
            .around(new FlakyTestRule())
            .around(new FailureVideoTestWatcher())
//.around(l);
//.around(new TestWatcher() {})
            .around(new ClapperboardTestWatcher())
            .around(new FailureScreenshotTestWatcher());

    //@Flaky
    @Test
    public void test_Failure(){
        //doIt("test1");
        Assert.fail("Intentional failure - as an example");
    }

    @Test
    public void test_Pass(){
        //doIt("test1");
        //Assert.fail("Intentional failure - as an example");
    }

    @Test
    public void test_Failure2(){
        //doIt("test1");
        Assert.fail("Intentional failure2 - as an example");
    }

    private void doIt(String s){
        try {
            Capture.newVideoRecording(s).finish();
        } catch (CaptureException e) {
            e.printStackTrace();
        }

        /*try {
            l.before();
            l.after();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }*/
    }

    /*
    //TODO: This style of repetition interacts with the FailureVideoTestWatcher in such a way that crashes the test instrumentation process
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
    */
}
