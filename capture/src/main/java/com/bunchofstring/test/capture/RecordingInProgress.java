package com.bunchofstring.test.capture;

import com.bunchofstring.test.CoreUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordingInProgress {

    private static final String CMD = "screenrecord %s --bit-rate 1000000";
    private static final String FILE_PATH = "%s.%s";
    private static final String EXTENSION = "mp4";
    private static final Logger LOGGER = Logger.getLogger(RecordingInProgress.class.getSimpleName());

    private final File file;

    RecordingInProgress(final File f) {
        try {
            file = new File(String.format(Locale.ENGLISH, FILE_PATH, f.getCanonicalPath(), EXTENSION));
        } catch(Throwable t) {
            throw new RuntimeException("Screen video capture operation failed", t);
        }
    }

    public void start() throws IOException{
        final String filePath = file.getCanonicalPath();
        final String screenRecordCmd = String.format(Locale.ENGLISH, CMD, filePath);
        LOGGER.log(Level.INFO, String.format("Starting screen video capture to %s", filePath));
        CoreUtils.executeShellCommand(screenRecordCmd);
    }

    public void finish(){
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

    public boolean cancel(){
        finish();
        if(!file.delete()){
            LOGGER.log(Level.WARNING, "Could not clean up the canceled screenshot at " + file);
            return false;
        }
        return true;
    }
}
