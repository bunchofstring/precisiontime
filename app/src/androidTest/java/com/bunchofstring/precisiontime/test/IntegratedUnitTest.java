package com.bunchofstring.precisiontime.test;

import com.bunchofstring.precisiontime.NtpTimestampProvider;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class IntegratedUnitTest {

    private static final String NEW_SOURCE = "NEW_SOURCE_FAKE";
    private NtpTimestampProvider ntp = new NtpTimestampProvider(-1L);

    @Test
    public void test_GivenNewSource_WhenSetSource_ThenUpdateSource() {
        //Given
        String previousSource = null;
        try {
            previousSource = ntp.getSource();
            assertNotEquals(NEW_SOURCE, previousSource);

            //When
            ntp.setSource(NEW_SOURCE);

            //Then
            assertEquals(NEW_SOURCE, ntp.getSource());
        } finally {
            //Cleanup
            ntp.setSource(previousSource);
        }
    }
}
