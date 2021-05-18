package com.bunchofstring.precisiontime;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public final class LabelMakerTest {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private final LabelMaker lm = new LabelMaker();

    @Test
    public void test_IntervalLabelContainsNumber() {
        final int num = 15;
        final String numString = String.valueOf(num);
        final String label = lm.getIntervalLabel(num, DEFAULT_LOCALE);
        Assert.assertTrue("Interval label should contain "+numString, label.contains(numString));
    }

    @Test
    public void test_SecondsToSyncLabelContainsNumber() {
        final int num = 6;
        final String numString = String.valueOf(num);
        final String label = lm.getSecondsToSyncLabel(num, DEFAULT_LOCALE);
        Assert.assertTrue("Seconds to sync label should contain "+numString+". It is '"+label+"'", label.contains(numString));
    }

    @Test
    public void test_TimeSinceSyncLabelContainsNumber() {
        final int num = 200;
        final String numString = String.valueOf(num);
        final String label = lm.getTimeSinceSyncLabel(num, DEFAULT_LOCALE);
        Assert.assertTrue("Time since sync label should contain "+numString, label.contains(numString));
    }

    @Test
    public void test_TimeLabelContainsNumber() {
        final long num = 448160400000L;
        final String numString = "1:00:00";
        final String label = lm.getTimeLabel(num, DEFAULT_LOCALE);
        Assert.assertTrue("Time label should contain "+numString+" "+label, label.contains(numString));
    }

    @Test
    public void test_TimeLabelContainsZone() {
        final String label = lm.getTimeLabel(0, DEFAULT_LOCALE);
        Assert.assertTrue("Time label should contain UTC", label.contains("UTC"));
    }

}