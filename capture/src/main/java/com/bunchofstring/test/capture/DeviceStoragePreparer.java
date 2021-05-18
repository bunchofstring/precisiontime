package com.bunchofstring.test.capture;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bunchofstring.test.CoreUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DeviceStoragePreparer {

    private static final Logger LOGGER = Logger.getLogger(DeviceStoragePreparer.class.getSimpleName());
    //Environment.getExternalStorageDirectory().getPath()
    private static final String DIR_DEVICE_DEFAULT = "/sdcard/Pictures/Captures";
    private static final String[] REQUIRED_PERMISSIONS = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    public static void grantPermissions() throws IOException {
        final String packageName = ApplicationProvider.getApplicationContext().getPackageName();
        CoreUtils.grantPermission(packageName, REQUIRED_PERMISSIONS);
    }

    public static String getDeviceDirPath() {
        final String deviceDirPath = InstrumentationRegistry.getArguments().getString("captureDeviceDir");
        if(deviceDirPath == null) {
            LOGGER.log(Level.WARNING, "Could not find device directory configuration. Using default instead: "+DIR_DEVICE_DEFAULT);
            return DIR_DEVICE_DEFAULT;
        }
        return deviceDirPath;
    }

    public static void ensureDeviceDirExists(final File dir) {
        if(!dir.mkdirs() && !dir.exists()){
            throw new RuntimeException("Could not create or access device directory at "+dir.getPath());
        }
    }

    public static File getDeviceSubdir(final String subDir){
        return new File(getDeviceDirPath().concat("/"+subDir+"/"));
    }
}
