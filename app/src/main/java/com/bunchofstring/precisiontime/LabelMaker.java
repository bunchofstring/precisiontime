package com.bunchofstring.precisiontime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class LabelMaker {

    private final static String TIME_ZONE = "UTC";

    private final static String UNDEFINED_TIME_LABEL = "?";
    private final static String DATE_FORMAT_PATTERN = "HH:mm:ss.SSS z";
    private final static String FORMAT_TIME_SINCE_SYNC = "Synced %d s ago";
    private final static String FORMAT_NEXT_SYNC = "Next sync in %d s";
    private final static String FORMAT_INTERVAL = "%d ms frame interval";

    public String getUndefinedTimeLabel(){
        return UNDEFINED_TIME_LABEL;
    }

    public String getTimeLabel(final long timeInMilliseconds, final Locale locale) {
        final Date date = new Date(timeInMilliseconds);
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN, locale);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
        return simpleDateFormat.format(date);
    }

    public String getSecondsToSyncLabel(final long countdownInSeconds, final Locale locale) {
        return String.format(locale, FORMAT_NEXT_SYNC, countdownInSeconds);
    }

    public String getTimeSinceSyncLabel(final long seconds, final Locale locale) {
        return String.format(locale, FORMAT_TIME_SINCE_SYNC, seconds);
    }

    public String getIntervalLabel(final long diff, final Locale locale){
        return String.format(locale, FORMAT_INTERVAL, diff);
    }
}
