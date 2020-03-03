package com.bunchofstring.precisiontime.test;

import com.bunchofstring.precisiontime.NtpTimestampProvider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NtpTimeStampProviderTest {

    private static final long DEFAULT_TIME_VALUE = -1;
    private static final String NEW_SOURCE = "NEW_SOURCE_FAKE";
    private NtpTimestampProvider ntp = new NtpTimestampProvider(DEFAULT_TIME_VALUE);

    @Test
    public void sourceManagement() {
        String previousSource = null;
        try {
            previousSource = ntp.getSource();
            ntp.setSource(NEW_SOURCE);
            assertEquals(NEW_SOURCE, ntp.getSource());
        } finally {
            ntp.setSource(previousSource);
        }

    }
}