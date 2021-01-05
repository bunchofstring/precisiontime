package com.bunchofstring.test.capture;

import android.util.Range;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FailureVideoTestWatcher extends TestWatcher {

    private static final Logger LOGGER = Logger.getLogger(FailureVideoTestWatcher.class.getSimpleName());

    private static final int ASSUMED_SCREENRECORD_LIMIT = 3 * 60 * 1000; //3 minutes
    private static final int DEFAULT_PADDING = 2000; //Two seconds

    private final int mPadding;
    private RecordingInProgress mRecordingInProgress;
    private boolean mIsDeactivated;
    private boolean mKeepSuccesses;

    public FailureVideoTestWatcher() {
        this(DEFAULT_PADDING);
    }

    /**
     * Test Watcher to capture a screen recording (i.e. video) of test failures. Recording cannot
     * exceed 3 minutes due to limitations of Android "screenrecord" - on which this functionality
     * is based.
     * @param padding Number of milliseconds to record before and after the test is executed. Note that
     *                this increases execution time
     */
    public FailureVideoTestWatcher(final int padding) {
        final Range<Integer> acceptableRange = new Range<>(0, ASSUMED_SCREENRECORD_LIMIT - 1);
        if(acceptableRange.contains(padding)){
            mPadding = padding;
        }else{
            mPadding = acceptableRange.getLower();
            LOGGER.log(Level.WARNING, String.format(Locale.getDefault(),
                    "The specified padding %d is outside the acceptable range (%d - %d). Falling back to lowest acceptable padding",
                    padding,
                    acceptableRange.getLower(),
                    acceptableRange.getUpper()));
        }
    }

    public FailureVideoTestWatcher keepSuccesses(final boolean keep){
        mKeepSuccesses = keep;
        return this;
    }

    @Override
    protected void starting(Description description) {
        LOGGER.log(Level.INFO, "Video recording starting for "+description+" padding="+mPadding);
        try {
            mRecordingInProgress = Capture.newVideoRecording(description.getClassName(), description.getMethodName());
            Thread.sleep(mPadding);
        } catch (CaptureException | InterruptedException e) {
            mIsDeactivated = true;
            LOGGER.log(Level.WARNING, "Problem capturing screen recording for the test", e);
        }
        super.starting(description);
    }

    @Override
    protected void failed(Throwable e, Description description){
        if(!mIsDeactivated){
            LOGGER.log(Level.INFO, "Video finishing for "+description);
            try {
                Thread.sleep(mPadding);
            } catch (InterruptedException e1) {
                LOGGER.log(Level.WARNING, "Recording was interrupted", e1);
            }
            mRecordingInProgress.finish();
        }
        super.failed(e, description);
    }

    @Override
    protected void succeeded(Description description) {
        if(!mIsDeactivated){
            LOGGER.log(Level.INFO, "Video recording cancelling for "+description);
            if(!mKeepSuccesses) {
                mRecordingInProgress.cancel();
            }else{
                try {
                    Thread.sleep(mPadding);
                } catch (InterruptedException e1) {
                    LOGGER.log(Level.WARNING, "Recording was interrupted", e1);
                }
                mRecordingInProgress.finish();
            }
        }
        super.succeeded(description);
    }
}
