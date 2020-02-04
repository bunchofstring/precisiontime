package com.example.precisiontimetest;


import androidx.annotation.NonNull;

interface TimestampProvider {
    void start();
    void stop();
    void restoreDefaultSource();
    void setSource(@NonNull String host);

    String getSource();
    long getTimestamp();
    long getSecondsToSync();
    long getSecondsSinceLastSync();
    boolean isSyncing();
}
