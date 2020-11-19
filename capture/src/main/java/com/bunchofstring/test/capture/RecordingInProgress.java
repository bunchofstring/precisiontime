package com.bunchofstring.test.capture;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.bunchofstring.test.CoreUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordingInProgress {

    private static final Logger LOGGER = Logger.getLogger(RecordingInProgress.class.getSimpleName());
    private static final String CMD = "screenrecord %s --bit-rate 1000000";

    private HandlerThread mHandlerThread;
    private boolean isRunning;

    private final File file;

    RecordingInProgress(final File f) {
        file = f;
        start();
    }

    public void finish(){
        LOGGER.log(Level.INFO, "Finishing screen recording at " + file);
    //    enqueue(() -> {
            killScreenRecorderProcesses();
    //    });
    }

    public void cancel(){
        LOGGER.log(Level.INFO, "Cancelling screen recording at " + file);
        finish();
    //    enqueue(() -> {
            try {
                LOGGER.log(Level.INFO, "Cleaning up screen recording at " + file);
                Files.delete(file.toPath());
                Files.delete(file.getParentFile().toPath());
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not clean up the canceled screen recording at " + file, e);
            }
    //    });
    }

    private void killScreenRecorderProcesses(){
        String [] pids;

        try {
            pids = CoreUtils.executeShellCommand("pidof screenrecord").split(" ");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not find the screenrecord process " + file, e);
            return;
        }

        for(String pid : pids){
            if(pid != null && !pid.isEmpty()){
                try {
                    CoreUtils.executeShellCommand(String.format("kill -2 %s", pid));
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Could not kill the screenrecord process " + file, e);
                }
            }
        }
    }

    @WorkerThread
    private void start(){
        enqueue(() -> {
            try{
                final String filePath = file.getCanonicalPath();
                final String screenRecordCmd = String.format(Locale.ENGLISH, CMD, filePath);
                LOGGER.log(Level.INFO, String.format("Starting screen video capture to %s", filePath));
                CoreUtils.executeShellCommand(screenRecordCmd);
                LOGGER.log(Level.INFO, String.format("Started screen video capture to %s", filePath));
            } catch (Throwable t) {
                throw new RuntimeException("Screen video capture operation failed", t);
            }
        });
    }

    private void enqueue(@NonNull Runnable r){
        if(!isRunning){
            mHandlerThread = new HandlerThread(Capture.class.getSimpleName());
            mHandlerThread.start();
            isRunning = true;
        }
        new Handler(mHandlerThread.getLooper()).post(r);
    }
}
