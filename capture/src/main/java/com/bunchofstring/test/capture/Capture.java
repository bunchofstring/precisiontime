package com.bunchofstring.test.capture;

import com.bunchofstring.test.CoreUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Capture {

    private static final Logger LOGGER = Logger.getLogger(Capture.class.getSimpleName());
    private static final String VIDEO_SUFFIX = ".mp4";
    private static final String IMAGE_SUFFIX = ".png";

    public static boolean screenshot(final String fileName){
        return screenshot("./", fileName);
    }

    public static boolean screenshot(final String subDirName, final String fileName){
        LOGGER.log(Level.INFO, "Attempting screenshot...");
        final File dir = DeviceStoragePreparer.getDeviceSubdir(subDirName);
        try {
            DeviceStoragePreparer.grantPermissions();
            DeviceStoragePreparer.ensureDeviceDirExists(dir);
            doScreenshot(dir, fileName);
            return true;
        } catch (Throwable throwable) {
            LOGGER.log(Level.WARNING, "Unable to capture screenshot", throwable);
            return false;
        }
    }

    public static RecordingInProgress newVideoRecording(final String fileName) {
        return newVideoRecording("./", fileName);
    }

    public static RecordingInProgress newVideoRecording(final String subDirName, final String fileName){
        LOGGER.log(Level.INFO, "Attempting video recording...");
        final File dir = DeviceStoragePreparer.getDeviceSubdir(subDirName);

        try {
            DeviceStoragePreparer.grantPermissions();
            DeviceStoragePreparer.ensureDeviceDirExists(dir);
            return doScreenrecord(dir, fileName);
        } catch (Throwable throwable) {
            throw new RuntimeException("Unable to capture video recording", throwable);
        }
    }

    private static RecordingInProgress doScreenrecord(final File dir, final String fileName) throws IOException {
        final Path path = Files.createTempFile(dir.toPath(), fileName, VIDEO_SUFFIX);
        return new RecordingInProgress(path.toFile());
    }

    private static void doScreenshot(final File dir, final String fileName) throws IOException {
        final File file = File.createTempFile(fileName, IMAGE_SUFFIX, dir);
        if (CoreUtils.getDevice().takeScreenshot(file)) {
            LOGGER.log(Level.INFO, "Captured screenshot at " + file.getAbsolutePath());
        } else {
            throw new RuntimeException("Screenshot capture operation failed");
        }
    }
}
