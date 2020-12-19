package com.bunchofstring.precisiontime.test.pageobject;

import com.bunchofstring.precisiontime.test.core.TestConfig;
import com.bunchofstring.test.CoreUtils;

import java.io.IOException;
import java.util.logging.Logger;

public class AppPageObject {

    private static final Logger LOGGER = Logger.getLogger(AppPageObject.class.getSimpleName());

    public static void launch(){
        CoreUtils.launchApp(TestConfig.PACKAGE_NAME);
    }

    public static void kill() throws IOException {
        CoreUtils.killApp(TestConfig.PACKAGE_NAME);
    }

    public static void reset() throws IOException {
        CoreUtils.resetApp(TestConfig.PACKAGE_NAME);
    }
}
