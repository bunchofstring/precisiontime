package com.bunchofstring.test.capture;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.logging.Level;
import java.util.logging.Logger;


public class ClapperboardTestWatcher extends TestWatcher {

    private static final Logger LOGGER = Logger.getLogger(ClapperboardTestWatcher.class.getSimpleName());

    private static final int ASSUMED_VISIBILITY_DURATION = 2000; //2 seconds

    @Override
    protected void starting(Description description) {
        toast(description.getMethodName());
    }

    private void toast(final String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Context context =  InstrumentationRegistry.getInstrumentation().getTargetContext();
            Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
        });
        try {
            Thread.sleep(ASSUMED_VISIBILITY_DURATION);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Clapper was interrupted", e);
        }
    }
}
