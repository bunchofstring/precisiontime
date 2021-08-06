package com.bunchofstring.precisiontime.core;

import androidx.annotation.NonNull;

import com.instacart.library.truetime.TrueTimeRx;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.reactivex.Single;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public final class NtpTimestampProvider implements TimestampProvider {

    private static final Logger LOGGER = Logger.getLogger(NtpTimestampProvider.class.getSimpleName());

    private static final String DEFAULT_NTP_HOST = "pool.ntp.org";

    private final TrueTimeRx tt = TrueTimeRx.build().withLoggingEnabled(false);
    private final GregorianCalendar currentTime = new GregorianCalendar();
    private final SyncOrchestrator syncOrchestrator = new SyncOrchestrator(this::sync);

    private String host = DEFAULT_NTP_HOST;
    private boolean isActive;
    private boolean isSyncInProgress;
    private Date lastSyncTimestamp;
    DisposableSingleObserver<Date> syncOperation;

    @Override
    public void start() {
        if(!isActive) {
            isActive = true;
            LOGGER.log(Level.INFO, "Starting periodic sync");
            sync();
        } else {
            LOGGER.log(Level.WARNING, "Periodic sync is already started");
        }
    }

    @Override
    public void stop() {
        LOGGER.log(Level.INFO, "Stopping periodic sync");
        isActive = false;
        cancelSync();
        syncOrchestrator.cancelScheduledSync();
    }

    @Override
    public long getTimestamp() throws UnreliableTimeException {
        return getReliableDate().getTime();
    }

    @Override
    public long getSecondsToSync() throws UnreliableTimeException {
        final Date now = getReliableDate();
        final long millisecondsUntilSync = syncOrchestrator.getMillisecondsUntilSync(now);
        return millisecondsToSeconds(millisecondsUntilSync);
    }

    @Override
    public long getSecondsSinceLastSync() throws UnreliableTimeException {
        long diff = (getReliableDate().getTime() - lastSyncTimestamp.getTime());
        return millisecondsToSeconds(diff);
    }

    @Override
    public boolean isSyncInProgress() {
        return isSyncInProgress;
    }

    @Override
    public void setSource(String host) {
        LOGGER.log(Level.INFO,"Setting source to "+host);
        if(!host.equals(this.host)) {
            this.host = host;
            if(isActive) {
                cancelSync();
                sync();
            }
        }
    }

    @Override
    public void restoreDefaultSource() {
        setSource(DEFAULT_NTP_HOST);
    }

    @Override
    public String getSource(){
        return host;
    }

    private Date getReliableDate() throws UnreliableTimeException {
        assertReliable();
        return TrueTimeRx.now();
    }

    private void assertReliable() throws UnreliableTimeException {
        if(!isReliable()) {
            throw new UnreliableTimeException();
        }
    }

    private boolean isReliable(){
        if(lastSyncTimestamp == null || !TrueTimeRx.isInitialized()){
            return false;
        }else{
            final long currentTime = TrueTimeRx.now().getTime();
            final long elapsedSinceLastSync = currentTime - lastSyncTimestamp.getTime();
            return elapsedSinceLastSync < TimestampProvider.SYNC_INTERVAL_MS;
        }
    }

    private void onError(Throwable t){
        cancelSync();
        sync();
        LOGGER.log(Level.SEVERE, "Problem fetching network time", t);
    }

    private void onSync(Date now){
        LOGGER.log(Level.INFO, "Synchronized current time "+now);
        setCurrentTime(now);
        cancelSync();
        syncOrchestrator.scheduleNextSync(now);
    }

    private void setCurrentTime(Date now){
        lastSyncTimestamp = now;
        currentTime.setTime(now);
    }

    private long millisecondsToSeconds(long milliseconds){
        return TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        //return (long)(((float) milliseconds) / 1000f);
    }

    private void sync(){
        if(!isSyncInProgress) {
            LOGGER.log(Level.INFO, "Syncing with source " + host);
            isSyncInProgress = true;
            syncOperation = newSyncOperation(host);
        }
    }

    private void cancelSync(){
        isSyncInProgress = false;
        syncOperation.dispose();
    }

    private DisposableSingleObserver<Date> newSyncOperation(final String host){
        final DisposableSingleObserver<Date> syncObserver = new DisposableSingleObserver<Date>() {
            @Override
            public void onSuccess(@NonNull Date date) {
                //Log.d(TAG, "Success initialized TrueTime :" + date.toString());
                isSyncInProgress = false;
                NtpTimestampProvider.this.onSync(date);
                dispose();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                //Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", e);
                isSyncInProgress = false;
                NtpTimestampProvider.this.onError(e);
                dispose();
            }
        };
        final Single<Date> single = TrueTimeRx.isInitialized() ?
                tt.initializeNtp(host).map(longs -> TrueTimeRx.now()) :
                tt.initializeRx(host);
        return single
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                //Note: Method reference does not work for these subscribers
                .subscribeWith(syncObserver);
    }
}
