package com.bunchofstring.precisiontime;

import com.bunchofstring.test.LifecycleTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.junit.Assert.*;

public class NtpTimeStampProviderDefaultsAndSourceManagementTest {

    private static final String SAMPLE_HOST = "sample_host";
    private NtpTimestampProvider ntp;

    @Rule
    public TestRule rule = new LifecycleTestRule() {
        @Override
        public void before() throws Throwable {
            ntp = new NtpTimestampProvider();
        }

        @Override
        public void after() throws Throwable {
            ntp = null;
        }
    };

    @Test
    public void test_NewInstanceIsDormant() {
        Assert.assertFalse("Premature sync", ntp.isSyncing());
    }

    @Test
    public void test_GivenNewInstance_ThenTimestampIsUnreliable(){
        try {
            ntp.getTimestamp();
            fail("Returned an unreliable value for timestamp");
        } catch (UnreliableTimeException e) {
            //No implementation
        }
    }

    @Test
    public void test_GivenNewInstance_ThenSecondsSinceLastSyncIsUnreliable() {
        try {
            ntp.getSecondsSinceLastSync();
            fail("Returned an unreliable value for seconds since last sync");
        } catch (UnreliableTimeException e) {
            //No implementation
        }
    }

    @Test
    public void test_GivenNewInstance_ThenSecondsToSyncIsUnreliable() {
        try {
            ntp.getSecondsToSync();
            fail("Returned an unreliable value for seconds to sync");
        } catch (UnreliableTimeException e) {
            //No implementation
        }
    }

    @Test
    public void test_GivenNewInstance_ThenDefaultSourceNotNull() {
        assertNotNull(ntp.getSource());
    }

    @Test
    public void test_GivenSourceChanged_CanRestoreDefault() {
        final String defaultSource = ntp.getSource();
        ntp.setSource(SAMPLE_HOST);
        ntp.restoreDefaultSource();
        Assert.assertEquals("Default source was not restored", defaultSource, ntp.getSource());
    }

    @Test
    public void test_CanChangeSource() {
        ntp.setSource(SAMPLE_HOST);
        Assert.assertEquals("Could not change the source", SAMPLE_HOST, ntp.getSource());
    }
}