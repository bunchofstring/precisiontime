package com.bunchofstring.precisiontime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class LabelMaker {

    public final static String UNDEFINED_TIME_LABEL = "?";
    public final static String INVISIBLE_LABEL = "";

    public String getTimeLabel(long timeInMilliseconds, Locale locale) {
        Date date = new Date(timeInMilliseconds);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS z", locale);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(date);
    }

    public String getSecondsToSyncLabel(long countdownInSeconds, Locale locale) {
        return String.format(locale, "Next sync in %d s", countdownInSeconds);
    }

    public String getTimeSinceSyncLabel(long seconds, Locale locale) {
        return String.format(locale, "Synced %d s ago", seconds);
    }

    public String getIntervalLabel(long diff, Locale locale){
        return String.format(locale, "%d ms frame interval", diff);
    }
}
