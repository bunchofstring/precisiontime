package com.bunchofstring.precisiontime.test;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.bunchofstring.precisiontime.NtpTimestampProvider;
import com.bunchofstring.precisiontime.test.core.PerformanceTests;
import com.bunchofstring.test.flaky.Flaky;
import com.bunchofstring.test.flaky.FlakyTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IntegratedUnitTest {

    private static final String NEW_SOURCE = "NEW_SOURCE_FAKE";
    private NtpTimestampProvider ntp = new NtpTimestampProvider(-1L);
    private final String previousSource = ntp.getSource();

    @Rule
    public FlakyTestRule flakyTestRule = new FlakyTestRule();

    @Test
    public void test_GivenNewSource_WhenSetSource_ThenUpdateSource() {
        try {
            //Given
            assertNotEquals(NEW_SOURCE, previousSource);

            //When
            ntp.setSource(NEW_SOURCE);

            //Then
            assertEquals(NEW_SOURCE, ntp.getSource());
        } finally {
            //Cleanup
            if(previousSource != null) {
                ntp.setSource(previousSource);
            }
        }
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

    @Category(PerformanceTests.class)
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
        System.out.println("talldave debug memory report took " + (startTime - new Date().getTime()) + " ms");

        StringJoiner j = new StringJoiner("\n", " \n", "");
        for(Map.Entry<String, String> map: mi.getMemoryStats().entrySet()){
            j.add(map.getKey() + " = " + map.getValue());
        }
        return j.toString();
    }

    private String getActivityManagerMemoryReport(){
        long startTime = new Date().getTime();
        ActivityManager.MemoryInfo mi2 = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) ApplicationProvider.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        assertNotNull("Could not determine memory usage", activityManager);
        activityManager.getMemoryInfo(mi2);
        System.out.println("talldave am memory report took " + (startTime - new Date().getTime()) + " ms");
        return new StringJoiner("\n"," \n","")
                .add("totalMem = " + ((float) mi2.totalMem/1024) + "K")
                .add("availMem = " + ((float) mi2.availMem/1024) + "K")
                .add("threshold = " + ((float) mi2.threshold/1024) + "K  <-- Low Memory Killer")
                .add("lowMemory = " + mi2.lowMemory)
                .toString();
    }
}
