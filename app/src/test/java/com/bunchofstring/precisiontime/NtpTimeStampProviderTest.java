package com.bunchofstring.precisiontime;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class NtpTimeStampProviderTest {

    private static final long DEFAULT_TIME_VALUE = -1;
    private NtpTimestampProvider ntp = new NtpTimestampProvider(DEFAULT_TIME_VALUE);

    @Test
    public void defaultTimestamp() {
        assertEquals(DEFAULT_TIME_VALUE, ntp.getTimestamp());
    }

    @Test
    public void defaultSecondsSinceLastSync() {
        assertEquals(DEFAULT_TIME_VALUE, ntp.getSecondsSinceLastSync());
    }

    @Test
    public void defaultSecondsToSync() {
        assertEquals(DEFAULT_TIME_VALUE, ntp.getSecondsToSync());
    }

    @Test
    public void defaultSource() {
        assertNotNull(ntp.getSource());
    }
}