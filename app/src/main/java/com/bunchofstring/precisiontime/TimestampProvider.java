package com.bunchofstring.precisiontime;


import androidx.annotation.NonNull;

interface TimestampProvider {
    void start();
    void stop();
    void restoreDefaultSource();
    void setSource(@NonNull String host);

    String getSource();
    long getTimestamp() throws UnreliableTimeException;
    long getSecondsToSync() throws UnreliableTimeException;
    long getSecondsSinceLastSync() throws UnreliableTimeException;
    boolean isSyncing();
}
