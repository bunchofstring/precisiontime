package com.bunchofstring.precisiontime.test.pageobject;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.bunchofstring.precisiontime.test.core.TestConfig;
import com.bunchofstring.test.CoreUtils;

import java.io.IOException;

public final class AppPageObject {

    public static void launch(){
        CoreUtils.launchApp(TestConfig.PACKAGE_NAME);
    }

    public static void launchWithHost(final String host) {
        final Context context = ApplicationProvider.getApplicationContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(TestConfig.PACKAGE_NAME);
        intent.putExtra("KEY_HOST", host);
        context.startActivity(intent);
    }

    public static void kill() throws IOException {
        CoreUtils.killApp(TestConfig.PACKAGE_NAME);
    }

    public static void reset() throws IOException {
        CoreUtils.resetApp(TestConfig.PACKAGE_NAME);
    }
}
