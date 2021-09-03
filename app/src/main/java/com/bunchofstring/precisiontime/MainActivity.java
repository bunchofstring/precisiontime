package com.bunchofstring.precisiontime;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import com.bunchofstring.precisiontime.core.NtpTimestampProvider;
import com.bunchofstring.precisiontime.core.TimestampProvider;
import com.bunchofstring.precisiontime.core.UnreliableTimeException;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String KEY_HOST = "KEY_HOST";

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    protected TimestampProvider timestampProvider = new NtpTimestampProvider(); /* TODO: This should be final. Only variable so a test can override it :( */
    private final ValueAnimator animator = new ValueAnimator();

    private long previousFrameTimestamp;
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

//        final String intendedHost = getHostFromIntent(getIntent());
//        final String rememberedHost = getRememberedHost();
//        if(Objects.nonNull(intendedHost)){
//            hostField.setText(intendedHost);
//            timestampProvider.setSource(intendedHost);
//        }else if(Objects.nonNull(rememberedHost)){
//            hostField.setText(rememberedHost);
//            timestampProvider.setSource(rememberedHost);
//        }else{
//            hostField.setText(timestampProvider.getSource());
//        }

        final String host = getPresetHost().orElse(timestampProvider.getSource());
        hostField.setText(host);
        timestampProvider.setSource(host);
        timestampProvider.start();
    }

    private Optional<String> getPresetHost(){
        final String[] potentialHosts = {
                getHostFromIntent(getIntent()),
                getRememberedHost()
        };
        return Stream.of(potentialHosts)
                .filter(Objects::nonNull)
                .findFirst();
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
        final LabelMaker labelMaker = new LabelMaker();
        final Locale locale = Locale.getDefault();
        final long diff = value - previousFrameTimestamp;
        previousFrameTimestamp = value;

        //Update
        final String intervalLabel = (diff > 0) ?
                labelMaker.getIntervalLabel(diff, locale) :
                labelMaker.getUndefinedTimeLabel();
        frameSpacingLabel.setText(intervalLabel);
        try {
            currentTimeLabel.setText(labelMaker.getTimeLabel(timestampProvider.getTimestamp(), locale));
            syncCountdownLabel.setText(labelMaker.getSecondsToSyncLabel(timestampProvider.getSecondsToSync(), locale));
            timeSinceSyncLabel.setText(labelMaker.getTimeSinceSyncLabel(timestampProvider.getSecondsSinceLastSync(), locale));
        } catch (UnreliableTimeException e) {
            currentTimeLabel.setText(labelMaker.getUndefinedTimeLabel());
            syncCountdownLabel.setText(labelMaker.getUndefinedTimeLabel());
            timeSinceSyncLabel.setText(null);
        }

        //Show or hide
        final boolean isTimeSyncing = timestampProvider.isSyncInProgress();
        syncCountdownLabel.setVisibility((isTimeSyncing) ? View.GONE : View.VISIBLE);
        syncStatus.setVisibility((isTimeSyncing) ? View.VISIBLE : View.GONE);
    }

    private boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            //hostField.clearFocus();
            //new Handler(getMainLooper()).postDelayed(() -> findViewById(R.id.outer_container).requestFocus(), 1L);
new Handler(getMainLooper()).postDelayed(() -> hostField.clearFocus(), 10L);

            final String host = ((EditText) v).getText().toString();
            setRememberedHost(host);
            timestampProvider.setSource(host);
        }
        return false;
    }

    //Primarily used for testing purposes
    private String getHostFromIntent(final Intent intent){
        final String requestedHost = intent.getStringExtra(KEY_HOST);
        Log.d(TAG,"getHostFromIntent(...) host="+requestedHost);
        return requestedHost;
    }

    private String getRememberedHost(){
        final SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        final String rememberedHost = sp.getString(KEY_HOST, null);
        Log.d(TAG,"getRememberedHost(...) host="+rememberedHost);
        return rememberedHost;
    }

    private void setRememberedHost(final String host){
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        sp.edit().putString(KEY_HOST, host).apply();
    }
}
