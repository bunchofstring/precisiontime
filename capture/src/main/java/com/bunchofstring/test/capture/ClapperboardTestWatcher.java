package com.bunchofstring.test.capture;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClapperboardTestWatcher extends TestWatcher {

    @Override
    protected void starting(Description description) {
        toast(description.getMethodName());
    }

    private void toast(final String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Context context =  InstrumentationRegistry.getInstrumentation().getTargetContext();
            Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
        });
    }
}
