package com.bunchofstring.precisiontime;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class NtpTimeStampProviderTest {

    private NtpTimestampProvider ntp = new NtpTimestampProvider();

    @Test
    public void defaultTimestamp() {
        try {
            ntp.getTimestamp();
            fail("Returned an unreliable value for timestamp");
        } catch (UnreliableTimeException e) {
            //No implementation
        }
    }

    @Test
    public void defaultSecondsSinceLastSync() {
        try {
            ntp.getSecondsSinceLastSync();
            fail("Returned an unreliable value for seconds since last sync");
        } catch (UnreliableTimeException e) {
            //No implementation
        }
    }

    @Test
    public void defaultSecondsToSync() {
        try {
            ntp.getSecondsToSync();
            fail("Returned an unreliable value for seconds to sync");
        } catch (UnreliableTimeException e) {
            //No implementation
        }
    }

    @Test
    public void defaultSource() {
        assertNotNull(ntp.getSource());
    }
}