package com.bunchofstring.test.capture;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.test.runner.screenshot.Screenshot;
import androidx.test.uiautomator.UiDevice;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;



public class CaptureTestWatcher extends TestWatcher {

    private static final Logger LOGGER = Logger.getLogger(CaptureTestWatcher.class.getSimpleName());
    private static final String ERROR_MESSAGE = "Screenshot capture operation failed";

    private final InstrumentedDeviceHelper mDevice;

    public CaptureTestWatcher(Instrumentation i){
        mDevice = new InstrumentedDeviceHelper(i);
    }

    @SuppressWarnings("unused")
    private CaptureTestWatcher() throws InstantiationException {
        throw new InstantiationException();
    }

    @Override
    protected void failed(Throwable e, Description description){
        super.failed(e, description);
        File baseFile = mDevice.getApplicationContext().getExternalFilesDir(null);
        doCapture(new File(baseFile, description.getMethodName()));
    }

    private void doCapture(File file){
        try {
            //Establish required permissions
            grantPermission("android.permission.READ_EXTERNAL_STORAGE");
            grantPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, ERROR_MESSAGE, e);
        }

        if(mDevice.takeScreenshot(file)) {
            LOGGER.log(Level.INFO, "Captured screenshot at "+file.getAbsolutePath());
        } else {
            LOGGER.log(Level.WARNING, ERROR_MESSAGE);
        }
    }

    private void grantPermission(String permission) throws IOException {
        final String command = "pm grant " + "com.bunchofstring.precisiontime " + permission;
        LOGGER.log(Level.INFO, "Executing command: "+command);
        final String output = mDevice.executeShellCommand(command);
        LOGGER.log(Level.INFO, "Command output: "+output);
    }
}
