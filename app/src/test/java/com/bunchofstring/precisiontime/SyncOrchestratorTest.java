package com.bunchofstring.precisiontime;

import com.bunchofstring.precisiontime.core.SyncOrchestrator;
import com.bunchofstring.precisiontime.core.TimestampProvider;
import com.bunchofstring.test.LifecycleTestRule;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mockito;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class SyncOrchestratorTest {

    private final SyncOrchestrator.Listener mockListener = Mockito.mock(SyncOrchestrator.Listener.class);
    private SyncOrchestrator so;

    @Rule
    public TestRule rule = new LifecycleTestRule() {
        @Override
        public void before() {
            so = new SyncOrchestrator(mockListener);
        }

        @Override
        public void after() {
            so = null;
        }
    };

    @Test
    public void test_SyncRequestedCallback() throws InterruptedException {
        so.scheduleNextSync(getEpochPlus(TimestampProvider.SYNC_INTERVAL_MS - 1));
        Thread.sleep(50);
        Mockito.verify(mockListener).onSyncRequested();
    }

    @Test
    public void test_SyncRequestedCallbackNegative() throws InterruptedException {
        so.scheduleNextSync(getEpochPlus(TimestampProvider.SYNC_INTERVAL_MS - 100));
        Thread.sleep(50);
        Mockito.verify(mockListener, Mockito.never()).onSyncRequested();
    }

    @Test
    public void test_ScheduleSyncOnTheTensInTenMinutes() {
        assertSchedule(minutes(10));
    }

    @Test
    public void test_ScheduleSyncOnTheTensInFiveMinutes() {
        assertSchedule(minutes(5));
    }

    @Test
    public void test_ScheduleSyncOnTheTensInOneMillisecond() {
        assertSchedule(1);
    }

    @Test
    public void test_SyncInOneSecond() {
        assertSchedule(1000);
    }

    private void assertSchedule(long offset){
        Assert.assertEquals("Incorrect milliseconds until next sync",
                TimestampProvider.SYNC_INTERVAL_MS - offset,
                so.getMillisecondsUntilSync(getEpochPlus(offset))
        );
    }

    private Date getEpoch(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        return cal.getTime();
    }

    private Date getEpochPlus(final long milliseconds){
        return new Date(getEpoch().getTime() + milliseconds);
    }

    private long minutes(final int minutes){
        return TimeUnit.MINUTES.toMillis(minutes);
    }
}