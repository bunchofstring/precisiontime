package com.bunchofstring.test.capture;

import com.bunchofstring.test.CoreUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Capture {

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


    public static RecordingInProgress newVideoRecording(final String fileName) throws CaptureException {
        return newVideoRecording("./", fileName);
    }

    public static RecordingInProgress newVideoRecording(final String subDirName, final String fileName) throws CaptureException {
        LOGGER.log(Level.INFO, "Attempting video recording...");
        final File dir = DeviceStoragePreparer.getDeviceSubdir(subDirName);

        try {
            DeviceStoragePreparer.grantPermissions();
            DeviceStoragePreparer.ensureDeviceDirExists(dir);
            return doScreenrecord(dir, fileName);
        } catch (Throwable throwable) {
            /*
            Not recoverable during during test execution, but a caller should anticipate this
            situation - because it directly affects the effectiveness of the tests. A runtime error
            in Capture should not affect the test - unless the caller wants it to.
            1. Force the caller to handle the Throwable. Test can decide what to do - in the moment :D
            2. Log the Throwable and swallow it. Test continues unaware :)
            3. Throw an unchecked exception. Test fails. Test can decide what to do - in the moment -
               but that would require catching an unchecked exception - which would be a bad code smell :|
            */
            throw new CaptureException("Unable to capture video recording", throwable);
        }
    }

    private static RecordingInProgress doScreenrecord(final File dir, final String fileName) throws IOException {
        final Path path = Files.createTempFile(dir.toPath(), fileName + "_", VIDEO_SUFFIX);
        return new RecordingInProgress(path.toFile());
    }

    private static void doScreenshot(final File dir, final String fileName) throws IOException {
        final File file = File.createTempFile(fileName + "_", IMAGE_SUFFIX, dir);
        if (CoreUtils.getDevice().takeScreenshot(file)) {
            LOGGER.log(Level.INFO, "Captured screenshot at " + file.getAbsolutePath());
        } else {
            throw new RuntimeException("Screenshot capture operation failed");
        }
    }
}
