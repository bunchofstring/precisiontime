package com.bunchofstring.precisiontime;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private final static long UNDEFINED_TIMESTAMP = -1L;
    private final static String UNDEFINED_TIME_LABEL = "?";
    private final static String INVISIBLE_LABEL = "";
    private final static String KEY_HOST = "KEY_HOST";
    private final static TimestampProvider timestampProvider = new NtpTimestampProvider(UNDEFINED_TIMESTAMP);

    private final ValueAnimator animator = new ValueAnimator();

    private static long previousFrameTimestamp;
    private TextView currentTimeLabel;
    private TextView frameSpacingLabel;
    private TextView syncCountdownLabel;
    private TextView timeSinceSyncLabel;
    private View syncStatus;
    private EditText hostField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.ntp_host)).setText(getHost());

        animator.setObjectValues(0L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setEvaluator((fraction, startValue, endValue) -> new Date().getTime());
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentTimeLabel = findViewById(R.id.current_time);
        frameSpacingLabel = findViewById(R.id.delay_between_frames);
        syncCountdownLabel = findViewById(R.id.sync_countdown);
        timeSinceSyncLabel = findViewById(R.id.last_sync);
        syncStatus = findViewById(R.id.status_actively_syncing);
        hostField = findViewById(R.id.ntp_host);

        hostField.setOnKeyListener(this::onKey);
        animator.addUpdateListener((animation) -> onFrameValue((long) animation.getAnimatedValue()));
        animator.start();
        timestampProvider.setSource(getHost());
        timestampProvider.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timestampProvider.stop();
        animator.end();
        animator.removeAllUpdateListeners();
        currentTimeLabel = null;
        frameSpacingLabel = null;
        syncCountdownLabel = null;
        timeSinceSyncLabel = null;
        hostField = null;
        syncStatus = null;
    }

    private String getHost(){
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        String defaultSource = timestampProvider.getSource();
        return sp.getString(KEY_HOST, defaultSource);
    }

    private void setHost(String host){
        Log.d(TAG,"setHost(...) host="+host);
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        sp.edit().putString(KEY_HOST, host).apply();
        hostField.setText(host);
        timestampProvider.setSource(host);
    }

    private void onFrameValue(long value){
        final Locale locale = Locale.getDefault();
        final boolean isTimeSyncing = timestampProvider.isSyncing();
        final long diff = value - previousFrameTimestamp;
        previousFrameTimestamp = value;

        //Update
        frameSpacingLabel.setText(getIntervalLabel(diff, locale));
        currentTimeLabel.setText(getTimeLabel(timestampProvider.getTimestamp(), locale));
        syncCountdownLabel.setText(getSecondsToSyncLabel(timestampProvider.getSecondsToSync(), locale));
        timeSinceSyncLabel.setText(getTimeSinceSyncLabel(timestampProvider.getSecondsSinceLastSync(), locale));

        //Show or hide
        syncCountdownLabel.setVisibility((isTimeSyncing) ? View.GONE : View.VISIBLE);
        syncStatus.setVisibility((isTimeSyncing) ? View.VISIBLE : View.GONE);
    }

    private String getIntervalLabel(long diff, Locale locale){
        if(diff == 0) return UNDEFINED_TIME_LABEL;
        return String.format(locale, "%d ms frame interval", diff);
    }

    private String getTimeLabel(long timeInMilliseconds, Locale locale) {
        if(timeInMilliseconds == UNDEFINED_TIMESTAMP) return UNDEFINED_TIME_LABEL;
        Date date = new Date(timeInMilliseconds);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS z", locale);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(date);
    }

    private String getSecondsToSyncLabel(long countdownInSeconds, Locale locale) {
        if (countdownInSeconds == UNDEFINED_TIMESTAMP) return UNDEFINED_TIME_LABEL;
        return String.format(locale, "Next sync in %d s", countdownInSeconds);
    }

    private String getTimeSinceSyncLabel(long seconds, Locale locale) {
        if (seconds == UNDEFINED_TIMESTAMP) return INVISIBLE_LABEL;
        return String.format(locale, "Synced %d s ago", seconds);
    }

    private boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            setHost(((EditText) v).getText().toString());
            hostField.clearFocus();
            new Handler(getMainLooper()).postDelayed(() -> findViewById(R.id.outer_container).requestFocus(), 1L);
        }
        return false;
    }
}
