package com.bunchofstring.precisiontime.core;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;

/**
 * App instances will attempt to sync at roughly the same time. For a set of different devices,
 * this helps minimize the total error due to drift.
 */

public final class SyncOrchestrator {

    private static final Logger LOGGER = Logger.getLogger(SyncOrchestrator.class.getSimpleName());

    private final GregorianCalendar currentTime = new GregorianCalendar();

    private final Listener listener;
    private Disposable timer;

    private SyncOrchestrator(){
        throw new UnsupportedOperationException("Default constructor is not supported");
    }

    public SyncOrchestrator(final Listener listener) {
        this.listener = listener;
    }

    public long getMillisecondsUntilSync(final Date now) {
        currentTime.setTime(now);
        int minute = currentTime.get(GregorianCalendar.MINUTE);
        int second = currentTime.get(GregorianCalendar.SECOND);
        int millisecond = currentTime.get(GregorianCalendar.MILLISECOND);
        int value = 1000 * 60 * minute + 1000 * second + millisecond;
        if(value == TimestampProvider.SYNC_INTERVAL_MS){
            return 0;
        }else {
            return TimestampProvider.SYNC_INTERVAL_MS - value % TimestampProvider.SYNC_INTERVAL_MS;
        }
    }

    public void scheduleNextSync(final Date now){
        disposeTimer();
        timer = Completable
                .timer(getMillisecondsUntilSync(now), TimeUnit.MILLISECONDS)
                .subscribe(() -> {
                    LOGGER.log(Level.INFO, "Next sync");
                    listener.onSyncRequested();
                });
    }

    public interface Listener {
        void onSyncRequested();
    }

    public void cancelScheduledSync(){
        disposeTimer();
    }

    private void disposeTimer(){
        if(timer != null) {
            timer.dispose();
            timer = null;
        }
    }
}
