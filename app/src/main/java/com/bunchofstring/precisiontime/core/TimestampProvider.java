package com.bunchofstring.precisiontime.core;


import androidx.annotation.NonNull;

public interface TimestampProvider {

    int SYNC_INTERVAL_MS = 1000 * 60 * 10;

    void start();
    void stop();
    void restoreDefaultSource();
    void setSource(@NonNull String host);

    String getSource();
    long getTimestamp() throws UnreliableTimeException;
    long getSecondsToSync() throws UnreliableTimeException;
    long getSecondsSinceLastSync() throws UnreliableTimeException;
    boolean isSyncInProgress();
}
