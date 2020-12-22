package com.bunchofstring.test.capture;

import android.os.ParcelFileDescriptor;
import android.os.Process;

import androidx.test.platform.app.InstrumentationRegistry;

import com.bunchofstring.test.CoreUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordingInProgress {

    private static final Logger LOGGER = Logger.getLogger(RecordingInProgress.class.getSimpleName());
    private static final String CMD = "screenrecord --bit-rate 1000000 --verbose %s";

    private final ExecutorService mQueue = Executors.newSingleThreadExecutor();
    private final File file;

    RecordingInProgress(final File f) {
        file = f;
        start();
    }

    boolean cancellationPending = false;

    public void finish(){
        mQueue.submit(() -> {
            LOGGER.log(Level.INFO, "Finishing screen recording at " + file);
            killScreenRecorderProcesses();
/*if(!cancellationPending) {
    gracefulEnd();
}*/
        });

        if(!cancellationPending) {
            gracefulEnd();
        }
    }

    public void cancel(){
        cancellationPending = true;
        finish();
        mQueue.submit(() -> {
            try {
                LOGGER.log(Level.INFO, "Cleaning up screen recording at " + file);
                Files.delete(file.toPath());
                Files.delete(file.getParentFile().toPath());
            } catch (DirectoryNotEmptyException dnee) {
                //No implementation
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not clean up the canceled screen recording at " + file, e);
            }
//gracefulEnd();
        });
        gracefulEnd();
    }

    private void gracefulEnd(){
        LOGGER.log(Level.INFO, "Graceful end " + file);
        mQueue.shutdown();
        try {
            mQueue.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
                pid = pid.trim();
                LOGGER.log(Level.INFO, "Killing screen video recording pid " + pid + " from pid " + Process.myPid());
                try {
                    CoreUtils.executeShellCommand(String.format("kill -2 %s", pid));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LOGGER.log(Level.INFO, "Killed screen video recording pid " + pid);
            }
        }
    }

    private void start(){
        mQueue.submit(() -> {
            try{
                final String filePath = file.getCanonicalPath();
                final String screenRecordCmd = String.format(Locale.ENGLISH, CMD, filePath);
                LOGGER.log(Level.INFO, String.format("Starting screen recording to %s", filePath));
//CoreUtils.executeShellCommand(screenRecordCmd);
ParcelFileDescriptor pfd = InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(screenRecordCmd);
FileInputStream fis = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
//fis.close();
                LOGGER.log(Level.INFO, String.format("Started screen recording to %s", filePath));

            } catch (Throwable t) {
                throw new RuntimeException("Screen video capture operation failed", t);
            }
        });
    }
}
