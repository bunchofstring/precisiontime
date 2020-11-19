package com.bunchofstring.test.capture;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FailureScreenshotTestWatcher extends TestWatcher {

    private static final Logger LOGGER = Logger.getLogger(FailureScreenshotTestWatcher.class.getSimpleName());

    //TODO: Constructor that takes an Instrumentation as an argument (PC-side test)

    @Override
    protected void failed(Throwable e, Description description){
        LOGGER.log(Level.INFO, "Screenshot required...");
        Capture.screenshot(description.getClassName(), description.getMethodName());
        super.failed(e, description);
    }
}