package com.example.precisiontimetest;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.os.Looper.getMainLooper;

public class MainActivity extends AppCompatActivity {

    private final static long UNDEFINED_TIMESTAMP = -1L;
    private final static String UNDEFINED_TIME_LABEL = "?";
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

    private String getHost(){
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        String defaultSource = timestampProvider.getSource();
        return sp.getString(KEY_HOST, defaultSource);
    }

    private void setHost(String host){
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        sp.edit().putString(KEY_HOST, host).apply();
        hostField.setText(host);
        timestampProvider.setSource(host);
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
        hostField.setOnFocusChangeListener((v, hasFocus) -> {
            setHost(((EditText) v).getText().toString());
        });
        hostField.setOnEditorActionListener((v, actionId, event) -> {
            if(EditorInfo.IME_ACTION_GO == actionId){
                //setHost(v.getText().toString());
                new Handler(getMainLooper()).postDelayed(() -> {
                    hostField.clearFocus();
                },1);
            }
            return false;
        });
        animator.addUpdateListener((animation) -> onFrameValue((long) animation.getAnimatedValue()));
        animator.start();
        timestampProvider.setSource(getHost());
        timestampProvider.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hostField.clearFocus();
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

    private void onFrameValue(long value){
        final Locale locale = Locale.getDefault();
        final long diff = value - previousFrameTimestamp;
        previousFrameTimestamp = value;
        frameSpacingLabel.setText(getIntervalLabel(diff, locale));

        currentTimeLabel.setText(getTimeLabel(timestampProvider.getTimestamp(), locale));
        syncCountdownLabel.setVisibility((timestampProvider.isSyncing()) ? View.GONE : View.VISIBLE);
        syncCountdownLabel.setText(getSecondsToSyncLabel(timestampProvider.getSecondsToSync(), locale));
        timeSinceSyncLabel.setText(getTimeSinceSyncLabel(timestampProvider.getSecondsSinceLastSync(), locale));
        syncStatus.setVisibility((timestampProvider.isSyncing()) ? View.VISIBLE : View.GONE);
    }

    private String getIntervalLabel(long diff, Locale locale){
        if(diff == 0) {
            return UNDEFINED_TIME_LABEL;
        }else{
            return String.format(locale, "%d ms frame interval", diff);
        }
    }

    private String getTimeLabel(long timeInMilliseconds, Locale locale) {
        if(timeInMilliseconds == UNDEFINED_TIMESTAMP){
            return UNDEFINED_TIME_LABEL;
        }else{
            //return String.format(locale, "%d ms", timeInMilliseconds);
            Date date = new Date(timeInMilliseconds);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS z", locale);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return simpleDateFormat.format(date);
        }

        /*Date date = new Date(timeInMilliseconds);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(""+
                "'hr':HH\n"+
                "'min':mm\n"+
                "'sec':ss\n"+
                "'ms':SSS", locale);
        return simpleDateFormat.format(date);*/
    }

    private String getSecondsToSyncLabel(long countdownInSeconds, Locale locale) {
        if (countdownInSeconds == UNDEFINED_TIMESTAMP) {
            return UNDEFINED_TIME_LABEL;
        } else {
            return String.format(locale, "Next sync in %d s", countdownInSeconds);
        }
    }

    private String getTimeSinceSyncLabel(long seconds, Locale locale) {
        if (seconds == UNDEFINED_TIMESTAMP) {
            return "";//UNDEFINED_TIME_LABEL;
        } else {
            return String.format(locale, "Synced %d s ago", seconds);
        }
    }
}
