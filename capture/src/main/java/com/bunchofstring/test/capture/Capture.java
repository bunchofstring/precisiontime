package com.bunchofstring.test.capture;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

import com.bunchofstring.test.CoreUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Capture {

    private static final Logger LOGGER = Logger.getLogger(Capture.class.getSimpleName());

    public static boolean screenshot(final String fileName){
        return screenshot("./", fileName);
    }

    public static boolean screenshot(final String subDirName, final String fileName){
        LOGGER.log(Level.INFO, "Attempting screenshot...");
        final File dir = DeviceStoragePreparer.getDeviceSubdir(subDirName);
        try {
            DeviceStoragePreparer.grantPermissions();
            DeviceStoragePreparer.ensureDeviceDirExists(dir);
            doScreenshot(dir, fileName);
            return true;
        } catch (Throwable throwable) {
            LOGGER.log(Level.WARNING, "Unable to capture screenshot", throwable);
            return false;
        }
    }

    public static RecordingInProgress newVideoRecording(final String fileName) {
        return newVideoRecording("./", fileName);
    }

    public static RecordingInProgress newVideoRecording(final String subDirName, final String fileName){
        LOGGER.log(Level.INFO, "Attempting video recording...");
        final File dir = DeviceStoragePreparer.getDeviceSubdir(subDirName);

        try {
            DeviceStoragePreparer.grantPermissions();
            DeviceStoragePreparer.ensureDeviceDirExists(dir);
            return doScreenrecord(dir, fileName);
        } catch (Throwable throwable) {
            throw new RuntimeException("Unable to capture video recording", throwable);
        }
    }

    private static RecordingInProgress doScreenrecord(final File dir, final String fileName){
        final File file = new File(dir, fileName);
        final RecordingInProgress r = new RecordingInProgress(file);
        enqueue(() -> {
            try {
                r.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return r;
    }

    private static final HandlerThread mHandlerThread = new HandlerThread(Capture.class.getSimpleName());
    private static boolean isRunning;

    public static void enqueue(@NonNull Runnable r){
        if(!isRunning){
            mHandlerThread.start();
            isRunning = true;
        }
        new Handler(mHandlerThread.getLooper()).post(r);
    }

    private static void doScreenshot(final File dir, final String fileName) {
        final File file = new File(dir, fileName);
        if (CoreUtils.getDevice().takeScreenshot(file)) {
            LOGGER.log(Level.INFO, "Captured screenshot at " + file.getAbsolutePath());
        } else {
            throw new RuntimeException("Screenshot capture operation failed");
        }
    }
}
