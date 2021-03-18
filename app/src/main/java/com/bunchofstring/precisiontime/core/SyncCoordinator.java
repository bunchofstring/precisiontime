package com.bunchofstring.precisiontime.core;

import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;

public class SyncCoordinator {
    /**
     * App instances will attempt to sync at roughly the same time. For a set of different devices,
     * this helps minimize the total error due to drift.
     */
    private void scheduleNextSync(){
        try {
            long orchestrated = getMillisecondsToSync();
            syncOrchestrator = Completable.timer(orchestrated, TimeUnit.MILLISECONDS).subscribe(() -> sync());
        } catch (UnreliableTimeException e) {
            e.printStackTrace();
        }
    }

    private long getMillisecondsToSync() throws UnreliableTimeException {
        getTimestamp();
        int minute = currentTime.get(GregorianCalendar.MINUTE);
        int second = currentTime.get(GregorianCalendar.SECOND);
        int millisecond = currentTime.get(GregorianCalendar.MILLISECOND);
        int value = 1000 * 60 * minute + 1000 * second + millisecond;
        return SYNC_INTERVAL_MS - value % SYNC_INTERVAL_MS;
    }
}
