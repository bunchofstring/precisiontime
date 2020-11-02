package com.bunchofstring.precisiontime.test.core;

import android.os.Bundle;

import androidx.test.runner.AndroidJUnitRunner;

public class CustomInstrumentationRunner extends AndroidJUnitRunner {
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        System.out.println("talldave - instrumentationrunner-based create");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        System.out.println("talldave - instrumentationrunner-based finish "+results.toString());
        //NTS: Original intent was to make sure the test had time to clean up before the target app is uninstalled.
        //Keeping it as a reminder that it might be be a useful place for long-running operations which run at the end,
        /*try {
            Thread.sleep(120_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        super.finish(resultCode, results);
    }
}
