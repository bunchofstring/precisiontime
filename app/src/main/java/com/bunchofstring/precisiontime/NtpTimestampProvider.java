package com.bunchofstring.precisiontime;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.instacart.library.truetime.TrueTimeRx;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NtpTimestampProvider implements TimestampProvider {

    private static final String TAG = NtpTimestampProvider.class.getSimpleName();

    private static final String DEFAULT_NTP_HOST = "time.nist.gov";
    private static final int SYNC_INTERVAL_MS = 1000 * 60 * 10;

    private final GregorianCalendar currentTime = new GregorianCalendar();
    private final long defaultTimeValue;

    private String host = DEFAULT_NTP_HOST;
    private boolean isStarted;
    private boolean isSyncing;
    private Date lastSyncTimestamp;
    private Disposable periodicSync;

    public NtpTimestampProvider(long defaultTimeValue){
        this.defaultTimeValue = defaultTimeValue;
    }

    @SuppressWarnings("unused")
    private NtpTimestampProvider() throws InstantiationException {
        throw new InstantiationException();
    }

    @Override
    public void start() {
        Log.d(TAG, "start()");
        if(!isStarted) {
            isStarted = true;
            sync();
        }
    }

    @Override
    public void stop() {
        Log.d(TAG, "stop()");
        isStarted = false;
        stopPeriodicSync();
    }

    @Override
    public long getTimestamp(){
        if(isReliable()){
            currentTime.setTime(TrueTimeRx.now());
            return currentTime.getTimeInMillis();
        }else{
            return defaultTimeValue;
        }
    }

    @Override
    public long getSecondsToSync(){
        if(isReliable()){
            return millisecondsToSeconds(getMillisecondsToSync());
        }else{
            return defaultTimeValue;
        }
    }

    @Override
    public long getSecondsSinceLastSync(){
        getTimestamp();
        if(isReliable()){
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(lastSyncTimestamp);
            long diff = (currentTime.getTimeInMillis() - cal.getTimeInMillis());
            return millisecondsToSeconds(diff);
        }else{
            return defaultTimeValue;
        }
    }

    @Override
    public boolean isSyncing() {
        return isSyncing;
    }

    @Override
    public void setSource(@NonNull String host) {
        Log.d(TAG, "setSource(...) host="+host);
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
        Log.v(TAG, "onSync(...) date="+date);
        isSyncing = false;
        lastSyncTimestamp = date;
        currentTime.setTime(date);
    }

    private void onError(Throwable t){
        Log.v(TAG, "onError(...) t="+t);
        isSyncing = false;
        t.printStackTrace();
    }

    private long getMillisecondsToSync(){
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

    @SuppressLint("CheckResult")
    private void sync(){
        boolean isInitialized = TrueTimeRx.isInitialized();
        Log.d(TAG, "sync() host="+host+" isInitialized="+isInitialized);
        isSyncing = true;

        TrueTimeRx tt = TrueTimeRx.build();
        if(isInitialized){
            //noinspection ResultOfMethodCallIgnored
            tt.initializeNtp(host)
                    .map(longs -> TrueTimeRx.now())
                    .subscribeOn(Schedulers.io())
                    //Method reference does not work for these subscribers
                    .subscribe(date -> onSync(date),throwable -> onError(throwable));
        }else{
            //noinspection ResultOfMethodCallIgnored
            tt.initializeRx(host)
                    .subscribeOn(Schedulers.io())
                    .subscribe(date -> onSync(date), throwable -> onError(throwable));
        }

        //App instances will attempt to sync at roughly the same time. For a set of different
        //devices, this helps minimize the total error due to drift.
        stopPeriodicSync();
        long orchestrated = getMillisecondsToSync();
        periodicSync = Completable.timer(orchestrated, TimeUnit.MILLISECONDS).subscribe(() -> sync());
    }

    private void stopPeriodicSync(){
        if(periodicSync != null){
            periodicSync.dispose();
        }
    }
}
