package com.bunchofstring.precisiontime;

import com.instacart.library.truetime.TrueTimeRx;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class NtpTimestampProvider implements TimestampProvider {

    private static final Logger LOGGER = Logger.getLogger(NtpTimestampProvider.class.getSimpleName());

    private static final String DEFAULT_NTP_HOST = "pool.ntp.org";
    private static final int SYNC_INTERVAL_MS = 1000 * 60 * 10;

    private final GregorianCalendar currentTime = new GregorianCalendar();

    private String host = DEFAULT_NTP_HOST;
    private boolean isStarted;
    private boolean isSyncing;
    private Date lastSyncTimestamp;
    private Disposable periodicSync;

    @Override
    public void start() {
        if(isStarted) {
            LOGGER.log(Level.WARNING, "Periodic sync is already started");
        } else {
            LOGGER.log(Level.INFO, "Starting periodic sync");
            isStarted = true;
            sync();
        }
    }

    @Override
    public void stop() {
        LOGGER.log(Level.INFO, "Stoping periodic sync");
        isStarted = false;
        stopPeriodicSync();
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
    public boolean isSyncing() {
        return isSyncing;
    }

    @Override
    public void setSource(String host) {
        LOGGER.log(Level.INFO,"Setting source to "+host);
        if(!host.equals(this.host)) {
            this.host = host;
            if(isStarted) {
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

    private void onSync(Date date){
        LOGGER.log(Level.INFO, "Synchronized current time "+date);
        lastSyncTimestamp = date;
        currentTime.setTime(date);

        //App instances will attempt to sync at roughly the same time. For a set of different
        //devices, this helps minimize the total error due to drift.
        stopPeriodicSync();
        try {
            long orchestrated = getMillisecondsToSync();
            periodicSync = Completable.timer(orchestrated, TimeUnit.MILLISECONDS).subscribe(() -> sync());
        } catch (UnreliableTimeException e) {
            e.printStackTrace();
        }
    }

    private void onError(Throwable t){
        isSyncing = false;
        LOGGER.log(Level.SEVERE, "Problem fetching network time", t);
    }

    private long getMillisecondsToSync() throws UnreliableTimeException {
        getTimestamp();
        int minute = currentTime.get(GregorianCalendar.MINUTE);
        int second = currentTime.get(GregorianCalendar.SECOND);
        int millisecond = currentTime.get(GregorianCalendar.MILLISECOND);
        int value = 1000 * 60 * minute + 1000 * second + millisecond;
        return SYNC_INTERVAL_MS - value % SYNC_INTERVAL_MS;
    }

    private long millisecondsToSeconds(long milliseconds){
        return (long)(((float) milliseconds) / 1000f);
    }

    private void sync(){
        isSyncing = true;

        TrueTimeRx tt = TrueTimeRx.build().withLoggingEnabled(true);
        boolean isInitialized = TrueTimeRx.isInitialized();

        if(isInitialized){
            LOGGER.log(Level.INFO, "Resyncing with source "+host);
            tt.initializeNtp(host)
                    .map(longs -> TrueTimeRx.now())
                    .subscribeOn(Schedulers.io())
                    //Method reference does not work for these subscribers
                    .subscribeWith(new DisposableSingleObserver<Date>() {
                        @Override
                        public void onSuccess(Date date) {
                            //Log.d(TAG, "Success initialized TrueTime :" + date.toString());
                            NtpTimestampProvider.this.onSync(date);
                        }

                        @Override
                        public void onError(Throwable e) {
                            //Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", e);
                            NtpTimestampProvider.this.onError(e);
                        }
                    });
        }else{
            LOGGER.log(Level.INFO, "Syncing with source "+host);
            tt.initializeRx(host)
                    .subscribeOn(Schedulers.io())
                    .subscribeWith(new DisposableSingleObserver<Date>() {
                        @Override
                        public void onSuccess(Date date) {
                            //Log.d(TAG, "Success initialized TrueTime :" + date.toString());
                            NtpTimestampProvider.this.onSync(date);
                        }

                        @Override
                        public void onError(Throwable e) {
                            //Log.e(TAG, "something went wrong when trying to initializeRx TrueTime", e);
                            NtpTimestampProvider.this.onError(e);
                        }
                    });
        }
    }

    private void stopPeriodicSync(){
        isSyncing = false;
        if(periodicSync != null){
            periodicSync.dispose();
        }
    }
}
