package com.bunchofstring.precisiontime.core;


import androidx.annotation.NonNull;

public interface TimestampProvider {
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
