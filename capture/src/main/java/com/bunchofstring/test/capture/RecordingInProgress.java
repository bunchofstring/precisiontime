package com.bunchofstring.test.capture;

import com.bunchofstring.test.CoreUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordingInProgress {

    private static final Logger LOGGER = Logger.getLogger(RecordingInProgress.class.getSimpleName());
    private static final String CMD = "screenrecord --bit-rate 1000000 --bugreport --verbose %s";

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
        mTeardown.submit(() -> {
            deleteTemporaryFiles();
        });
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
            Files.delete(mFile.getParentFile().toPath());
        } catch (DirectoryNotEmptyException dnee) {
            /*
            No implementation. We take the "tell, don't ask" approach to cleaning up a parent
            directory which MIGHT no longer be necessary. This catches in case the directory is
            (in fact) still necessary
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
            e.printStackTrace();
        }
    }

    private void killScreenRecorderProcess(final String pid){
        try {
            CoreUtils.executeShellCommand(String.format("kill -2 %s", pid));

            //TODO: Find a way to ensure that kill works EVERY time on the screen recorder
            //Sometimes kill does not work, so we run it twice to be sure
            //CoreUtils.executeShellCommand(String.format("kill -2 %s", pid));
        } catch (IOException e) {
            e.printStackTrace();
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
            List<String> difference = Arrays.asList(getScreenRecorderPids());
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
                final String filePath = mFile.getCanonicalPath();
                final String screenRecordCmd = String.format(Locale.ENGLISH, CMD, filePath);
                LOGGER.log(Level.INFO, String.format("Starting screen recording to %s", filePath));

                //Blocking
                CoreUtils.executeShellCommand(screenRecordCmd);
            } catch (Throwable t) {
                throw new RuntimeException("Screen video capture operation failed", t);
            } finally {
                mRecorder.shutdown();
            }
        });

        mTeardown.submit(() -> {
            LOGGER.log(Level.INFO, "FOOOOOO!!!");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "Recording was interrupted", e);
            }
            mPid = awaitScreenRecorderPid(current);

            LOGGER.log(Level.INFO, "BAAAAAR!!!");
        });
    }
}
