package com.bunchofstring.precisiontime.core;

public interface TimestampProvider {

    int SYNC_INTERVAL_MS = 1000 * 60 * 10;

    void start();
    void stop();
    void restoreDefaultSource();
    void setSource(String host);

    long getTimestamp() throws UnreliableTimeException;
    long getSecondsToSync() throws UnreliableTimeException;
    long getSecondsSinceLastSync() throws UnreliableTimeException;
    boolean isSyncInProgress();
    String getSource();
}
