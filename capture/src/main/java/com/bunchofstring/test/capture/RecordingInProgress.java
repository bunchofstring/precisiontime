package com.bunchofstring.test.capture;

import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.bunchofstring.test.CoreUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RecordingInProgress {

    private static final Logger LOGGER = Logger.getLogger(RecordingInProgress.class.getSimpleName());

    private static final int WIDTH_CONSTRAINT = 600;
    private static final String CMD = "screenrecord %s --size %dx%d --bit-rate 1000000 --verbose --bugreport --show-frame-time";

    private final ExecutorService mRecorder = Executors.newSingleThreadExecutor();
    private final ExecutorService mTeardown = Executors.newSingleThreadExecutor();
    private final File mFile;
    private String mPid;

    RecordingInProgress(final File f) {
        mFile = f;
        start();
    }

    public void cancel(){
        finish(true);
        mTeardown.submit(this::deleteTemporaryFiles);
        gracefulEnd(mTeardown);
        awaitGracefulEnd(mTeardown);
    }

    public void finish(){
        finish(false);
    }

    private void finish(final boolean skipGracefulEnd){
        mTeardown.submit(() -> {
            LOGGER.log(Level.INFO, "Finishing screen recording at " + mFile);
            killScreenRecorderProcess(mPid);
            awaitGracefulEnd(mRecorder);
            LOGGER.log(Level.INFO, "Finished screen recording at " + mFile);
        });

        if(!skipGracefulEnd) {
            gracefulEnd(mTeardown);
            awaitGracefulEnd(mTeardown);
            try {
                //TODO: Eliminate this ugly sleep!
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Recording was interrupted", e);
            }
        }
    }

    private void deleteTemporaryFiles(){
        try {
            LOGGER.log(Level.INFO, "Cleaning up screen recording at " + mFile);
            Files.delete(mFile.toPath());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not clean up the canceled screen recording at " + mFile, e);
        }

        try {
            final File parentFile = Objects.requireNonNull(mFile.getParentFile(), "Parent file cannot be null");
            Files.delete(parentFile.toPath());
        } catch (DirectoryNotEmptyException ignored) {
            /*
            No implementation. We take the "tell, don't ask" approach to cleaning up a parent
            directory which MIGHT no longer be necessary. This catches in case the directory is
            (in fact) still populated
            */
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not clean up the directory " + mFile, e);
        }
    }

    private void gracefulEnd(final ExecutorService executorService){
        LOGGER.log(Level.INFO, "Graceful end " + mFile + " " + executorService);
        executorService.shutdown();
    }

    private void awaitGracefulEnd(final ExecutorService executorService){
        try {
            executorService.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Graceful end was interrupted", e);
        }
    }

    private void killScreenRecorderProcess(final String pid){
        try {
            CoreUtils.executeShellCommand(String.format("kill -2 %s", pid));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not kill screenrecord process "+pid, e);
        }
    }

    private String [] getScreenRecorderPids(){
        String [] pids = new String[0];
        try {
            pids = CoreUtils.executeShellCommand("pidof screenrecord").split(" ");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not find the screenrecord process " + mFile, e);
        }
        return Arrays.stream(pids)
                .filter(Objects::nonNull)
                .map(String::trim)
                .toArray(String[]::new);
    }

    private String awaitScreenRecorderPid(final String [] current) {
        LOGGER.log(Level.INFO, "Current PIDs = "+String.join(", ", current));
        List<String> existing = Arrays.asList(current);
        while(true){
            List<String> difference = new ArrayList<>(Arrays.asList(getScreenRecorderPids()));
            difference.removeAll(existing);
            if(!difference.isEmpty()){
                return difference.get(0);
            }
        }
    }

    private void start(){
        String [] current = getScreenRecorderPids();

        mRecorder.submit(() -> {
            try{
                final String cmd = getScreenRecordCommand();
                //Blocking
                CoreUtils.executeShellCommand(cmd);
            } catch (Throwable t) {
                throw new RuntimeException("Screen video capture operation failed", t);
            } finally {
                mRecorder.shutdown();
            }
        });

        mTeardown.submit(() -> {
            try {
                //TODO: Eliminate this ugly sleep!
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Recording was interrupted", e);
            }
            mPid = awaitScreenRecorderPid(current);
        });
    }

    private String getScreenRecordCommand() throws IOException {
        final Point p = CoreUtils.getDevice().getDisplaySizeDp();
        final DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        final int widthActual = (int) Math.floor(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, p.x, dm));
        final int heightActual = (int) Math.floor(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, p.y, dm));

        //At least one emulator on Windows cannot do screenrecord for anything bigger (not even at native display resolution)
        final int heightIntermediate = (int) Math.floor((double) (WIDTH_CONSTRAINT * heightActual)/widthActual);
        //Ensure even number
        final int heightAdjusted = heightIntermediate - (heightIntermediate % 2);

        final String filePath = mFile.getCanonicalPath();
        LOGGER.log(Level.INFO, String.format("Prepared command for screen recording to %s", filePath));
        return String.format(Locale.ENGLISH, CMD, filePath, WIDTH_CONSTRAINT, heightAdjusted);
    }
}
