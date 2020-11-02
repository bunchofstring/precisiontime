package com.bunchofstring.test.capture;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;


public class FailureVideoTestWatcher extends TestWatcher {

    private static final Logger LOGGER = Logger.getLogger(FailureVideoTestWatcher.class.getSimpleName());
    private static RecordingInProgress mRecordingInProgress;

    //TODO: Constructor that takes an Instrumentation as an argument (PC-side test)

    @Override
    protected void starting(Description description) {
        LOGGER.log(Level.INFO, "Video recording starting for "+description.hashCode());
        mRecordingInProgress = Capture.newVideoRecording(description.getClassName(), description.getMethodName());
        super.starting(description);
    }

    @Override
    protected void failed(Throwable e, Description description){
        LOGGER.log(Level.INFO, "Video required...");
        mRecordingInProgress.finish();
        super.failed(e, description);
    }

    @Override
    protected void succeeded(Description description) {
        LOGGER.log(Level.INFO, "Video recording stopping for "+description);
        if(!mRecordingInProgress.cancel()){
            LOGGER.log(Level.WARNING, "Video recording cleanup failed for "+description);
        }
        super.succeeded(description);
    }
}
