package com.bunchofstring.precisiontime;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.bunchofstring.test.LifecycleTestRule;
import com.bunchofstring.test.flaky.Flaky;
import com.bunchofstring.test.flaky.FlakyTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NtpTimeStampProviderLifecycleTest {

    private static final String NEW_SOURCE = "NEW_SOURCE_FAKE";
    private NtpTimestampProvider ntp = new NtpTimestampProvider();

    @Rule
    public TestRule rule = RuleChain.emptyRuleChain()
        .around(new FlakyTestRule())
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
        Assert.assertTrue("Start did not initiate network time sync", ntp.isSyncing());
    }

    @Test
    public void test_CanStopPeriodicSync() {
        ntp.start();
        ntp.stop();
        Assert.assertFalse("Could not stop periodic network time synchronization", ntp.isSyncing());
    }

    @Flaky(iterations = 10, traceAllFailures = true, itemizeSummary = true)
    @Test
    public void test_NonDeterministic() {
        System.out.println("talldave - test");
        final int range = 10;
        final int cutoffAfter = 2;
        final int value = new Random().nextInt(range)+1;
        assertTrue(value + " is beyond the cutoff", value <= cutoffAfter);
    }

    //@Category(PerformanceTests.class)
    @Test
    public void test_GivenMemoryPressure_ThenOperateWithinThreshold() {
        //TODO: Set a threshold based on actual, tax the system, and take a measurement
        Log.d("performance (ActivityManager.MemoryInfo)", getActivityManagerMemoryReport());
        Log.d("performance (Debug.MemoryInfo)", getDebugMemoryReport());
    }

    private String getDebugMemoryReport(){
        long startTime = new Date().getTime();
        Debug.MemoryInfo mi = new Debug.MemoryInfo();
        Debug.getMemoryInfo(mi);

        StringJoiner j = new StringJoiner("\n", " \n", "");
        for(Map.Entry<String, String> map: mi.getMemoryStats().entrySet()){
            j.add(map.getKey() + " = " + map.getValue());
        }
        return j.toString();
    }

    private String getActivityManagerMemoryReport(){
        ActivityManager.MemoryInfo mi2 = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) ApplicationProvider.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        assertNotNull("Could not determine memory usage", activityManager);
        activityManager.getMemoryInfo(mi2);
        return new StringJoiner("\n"," \n","")
                .add("totalMem = " + ((float) mi2.totalMem/1024) + "K")
                .add("availMem = " + ((float) mi2.availMem/1024) + "K")
                .add("threshold = " + ((float) mi2.threshold/1024) + "K  <-- Low Memory Killer")
                .add("lowMemory = " + mi2.lowMemory)
                .toString();
    }
}
