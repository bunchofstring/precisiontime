package com.bunchofstring.precisiontime.core;

import com.instacart.library.truetime.CacheInterface;
import com.instacart.library.truetime.TrueTimeRx;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class NtpTimestampProvider implements TimestampProvider {

    private static final Logger LOGGER = Logger.getLogger(NtpTimestampProvider.class.getSimpleName());

    private static final String DEFAULT_NTP_HOST = "pool.ntp.org";
    private static final int SYNC_INTERVAL_MS = 1000 * 60 * 10;

    private final GregorianCalendar currentTime = new GregorianCalendar();

    private String host = DEFAULT_NTP_HOST;
    private boolean isActive;
    private boolean isSyncInProgress;
    private Date lastSyncTimestamp;
    private Disposable syncOrchestrator;
    DisposableSingleObserver<Date> syncOperation;

    TrueTimeRx tt = TrueTimeRx.build()
            .withLoggingEnabled(true)
            .withCustomizedCache(new CacheInterface() { //TODO: Remove because it does not seem to help with long monitor contention
                //Threadsafe as a precaution
                private final ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

                @Override
                public void put(String key, long value) {
                    map.put(key,value);
                }

                @Override
                public long get(String key, long defaultValue) {
                    return (map.containsKey(key)) ? map.get(key) : defaultValue;
                }

                @Override
                public void clear() {
                    map.clear();
                }
            });

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
        LOGGER.log(Level.INFO, "Stoping periodic sync");
        isActive = false;
        cancelSync();
        cancelScheduledSync();
    }

    @Override
    public long getTimestamp() throws UnreliableTimeException {
        assertReliable();
        currentTime.setTime(TrueTimeRx.now());
        return currentTime.getTimeInMillis();
    }

    @Override
    public long getSecondsToSync() throws UnreliableTimeException {
        assertReliable();
        return millisecondsToSeconds(getMillisecondsToSync());
    }

    @Override
    public long getSecondsSinceLastSync() throws UnreliableTimeException {
        getTimestamp();
        assertReliable();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(lastSyncTimestamp);
        long diff = (currentTime.getTimeInMillis() - cal.getTimeInMillis());
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
                cancelScheduledSync();
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

    private void assertReliable() throws UnreliableTimeException {
        if(!isReliable()) throw new UnreliableTimeException();
    }

    private boolean isReliable(){
        if(lastSyncTimestamp == null || !TrueTimeRx.isInitialized()){
            return false;
        }else{
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(lastSyncTimestamp);
            long elapsedSinceLastSync = currentTime.getTimeInMillis() - cal.getTimeInMillis();
            return elapsedSinceLastSync < SYNC_INTERVAL_MS;
        }
    }

    private void onError(Throwable t){
        cancelSync();
        scheduleNextSync();
        LOGGER.log(Level.SEVERE, "Problem fetching network time", t);
    }

    private void onSync(Date date){
        LOGGER.log(Level.INFO, "Synchronized current time "+date);
        lastSyncTimestamp = date;
        currentTime.setTime(date);
        cancelSync();
        scheduleNextSync();
    }



    private long millisecondsToSeconds(long milliseconds){
        return (long)(((float) milliseconds) / 1000f);
    }

    private void sync(){
        if(!isSyncInProgress) {
            LOGGER.log(Level.INFO, "Syncing with source " + host);
            isSyncInProgress = true;
            syncOperation = newSyncOperation(host);
        }
    }

    private DisposableSingleObserver<Date> newSyncOperation(final String host){
        final DisposableSingleObserver<Date> syncObserver = new DisposableSingleObserver<Date>() {
            @Override
            public void onSuccess(Date date) {
                //Log.d(TAG, "Success initialized TrueTime :" + date.toString());
                isSyncInProgress = false;
                NtpTimestampProvider.this.onSync(date);
                dispose();
            }

            @Override
            public void onError(Throwable e) {
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

    private void cancelSync(){
        isSyncInProgress = false;
        syncOperation.dispose();
    }

    private void cancelScheduledSync(){
        if(syncOrchestrator != null) {
            syncOrchestrator.dispose();
        }
    }
}
