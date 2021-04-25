package com.bunchofstring.precisiontime;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.Map;
import java.util.StringJoiner;

public class PerformanceTest {

    //@Category(PerformanceTests.class)
    @Test
    public void test_GivenMemoryPressure_ThenOperateWithinThreshold() {
        //TODO: Set a threshold based on actual. Fail if the measurement exceeds the threshold
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
        Assert.assertNotNull("Could not determine memory usage", activityManager);
        activityManager.getMemoryInfo(mi2);
        return new StringJoiner("\n"," \n","")
                .add("totalMem = " + ((float) mi2.totalMem/1024) + "K")
                .add("availMem = " + ((float) mi2.availMem/1024) + "K")
                .add("threshold = " + ((float) mi2.threshold/1024) + "K  <-- Low Memory Killer")
                .add("lowMemory = " + mi2.lowMemory)
                .toString();
    }
}
