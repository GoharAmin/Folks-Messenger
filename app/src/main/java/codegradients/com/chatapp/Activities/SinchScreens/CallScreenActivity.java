package codegradients.com.chatapp.Activities.SinchScreens;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.AudioPlayer;
import codegradients.com.chatapp.helper_classes.SinchService;

public class CallScreenActivity extends BaseActivity {

    static final String TAG = CallScreenActivity.class.getSimpleName();
    static final String ADDED_LISTENER = "addedListener";
    static final String VIEWS_TOGGLED = "viewsToggled";

    String callerName = "";

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private String mCallId;
    private boolean mAddedListener = false;
    private boolean mLocalVideoViewAdded = false;
    private boolean mRemoteVideoViewAdded = false;

    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;
    boolean mToggleVideoViewPositions = false;

    ImageView micIcon, speakerIcon;
    boolean isSpeakerEnabled = false;
    AudioController audioController;
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;
    private int field = 0x00000020;
    boolean isMicOn = true;

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener);
        savedInstanceState.putBoolean(VIEWS_TOGGLED, mToggleVideoViewPositions);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
        mToggleVideoViewPositions = savedInstanceState.getBoolean(VIEWS_TOGGLED);
    }

    DatabaseReference mDatabaseForCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.callscreen);

        callerName = getIntent().getStringExtra("callerName");

        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = findViewById(R.id.callDuration);
        mCallerName = findViewById(R.id.remoteUser);
        mCallState = findViewById(R.id.callState);
        speakerIcon = findViewById(R.id.speakerIcon);
        micIcon = findViewById(R.id.micIcon);
        FloatingActionButton endCallButton = findViewById(R.id.hangupButton);

        try {
            field = PowerManager.class.getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
        } catch (Throwable ignored) {
        }
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(field, getLocalClassName());

        endCallButton.setOnClickListener(v -> endCall());

        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);

        mDatabaseForCall = FirebaseDatabase.getInstance().getReference().child("callsRecords").child(mCallId);

        micIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setMode(AudioManager.MODE_IN_CALL);

                if (!audioManager.isMicrophoneMute()) {
                    isMicOn = false;
                    audioManager.setMicrophoneMute(true);
                    micIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_mic_off_24));
                } else {
                    isMicOn = true;
                    audioManager.setMicrophoneMute(false);
                    micIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_mic_24));
                }
            }
        });

        speakerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSpeakerEnabled){
                    isSpeakerEnabled = false;
                    audioController = getSinchServiceInterface().getAudioController();
                    audioController.disableSpeaker();

                    acquireWakeLock();

                    speakerIcon.setImageDrawable(getResources().getDrawable(R.drawable.speaker_off_white));

                } else {
                    isSpeakerEnabled = true;
                    audioController = getSinchServiceInterface().getAudioController();
                    audioController.enableSpeaker();

                    releaseWakeLock();

                    speakerIcon.setImageDrawable(getResources().getDrawable(R.drawable.speaker_on_white));
                }
            }
        });
    }

    @Override
    public void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            if (!mAddedListener) {

                if (call.getDetails().isVideoOffered()) {
                    call.addCallListener(new SinchCallListener());
                    mAddedListener = true;
                } else {
                    call.addCallListener(new SinchCallListenerAudio());
                    mAddedListener = true;
                }
            }
        } else {
            Log.e(TAG, "Started with invalid callId, Aborting.");
            finish();
        }

        updateUI();
    }

    private void updateUI() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {

            ImageView callTypeImg = findViewById(R.id.callTypeIcon);

            if (call.getDetails().isVideoOffered()) {
                callTypeImg.setImageDrawable(getResources().getDrawable(R.drawable.call_video_icon_per));
                speakerIcon.setImageDrawable(getResources().getDrawable(R.drawable.speaker_on_white));

                releaseWakeLock();

                //Toast.makeText(this, "Video", Toast.LENGTH_SHORT).show();

                audioController = getSinchServiceInterface().getAudioController();
                audioController.enableSpeaker();

                isSpeakerEnabled = true;

            } else {
                //Toast.makeText(this, "Audio", Toast.LENGTH_SHORT).show();
                callTypeImg.setImageDrawable(getResources().getDrawable(R.drawable.call_audio_icon_per));
                speakerIcon.setImageDrawable(getResources().getDrawable(R.drawable.speaker_off_white));

                acquireWakeLock();

                audioController = getSinchServiceInterface().getAudioController();
                audioController.disableSpeaker();

                isSpeakerEnabled = false;
            }

            mCallerName.setText(callerName);
            mCallState.setText(call.getState().toString());
            if (call.getDetails().isVideoOffered()) {
                if (call.getState() == CallState.ESTABLISHED) {
                    setVideoViewsVisibility(true, true);
                } else {
                    setVideoViewsVisibility(true, false);
                }
            } else {
                setVideoViewsVisibility(false, false);
            }
        } else {
            setVideoViewsVisibility(false, false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mDurationTask.cancel();
        mTimer.cancel();
        removeVideoViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
        updateUI();
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    private void endCall() {
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }

    private ViewGroup getVideoView(boolean localView) {
        if (mToggleVideoViewPositions) {
            localView = !localView;
        }
        return localView ? findViewById(R.id.localVideo) : findViewById(R.id.remoteVideo);
    }

    private void addLocalView() {
        if (mLocalVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            runOnUiThread(() -> {
                ViewGroup localView = getVideoView(true);
                localView.addView(vc.getLocalView());
                localView.setOnClickListener(v -> vc.toggleCaptureDevicePosition());
                mLocalVideoViewAdded = true;
                vc.setLocalVideoZOrder(!mToggleVideoViewPositions);
            });
        }
    }
    private void addRemoteView() {
        if (mRemoteVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            runOnUiThread(() -> {
                ViewGroup remoteView = getVideoView(false);
                remoteView.addView(vc.getRemoteView());
                remoteView.setOnClickListener((View v) -> {
                    removeVideoViews();
                    mToggleVideoViewPositions = !mToggleVideoViewPositions;
                    addRemoteView();
                    addLocalView();
                });
                mRemoteVideoViewAdded = true;
                vc.setLocalVideoZOrder(!mToggleVideoViewPositions);
            });
        }
    }


    private void removeVideoViews() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        try {
            VideoController vc = getSinchServiceInterface().getVideoController();
            if (vc != null) {
                runOnUiThread(() -> {
                    try {
                        ((ViewGroup)(vc.getRemoteView().getParent())).removeView(vc.getRemoteView());
                        ((ViewGroup)(vc.getLocalView().getParent())).removeView(vc.getLocalView());
                        mLocalVideoViewAdded = false;
                        mRemoteVideoViewAdded = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setVideoViewsVisibility(final boolean localVideoVisibile, final boolean remoteVideoVisible) {
        if (getSinchServiceInterface() == null)
            return;
        if (mRemoteVideoViewAdded == false) {
            addRemoteView();
        }
        if (mLocalVideoViewAdded == false) {
            addLocalView();
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            runOnUiThread(() -> {
                vc.getLocalView().setVisibility(localVideoVisibile ? View.VISIBLE : View.GONE);
                vc.getRemoteView().setVisibility(remoteVideoVisible ? View.VISIBLE : View.GONE);
            });
        }
    }

    private class SinchCallListenerAudio implements CallListener {

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");

            mDatabaseForCall.child("callStartingTime").setValue(String.valueOf(System.currentTimeMillis()));

            mAudioPlayer.stopProgressTone();
            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//            AudioController audioController = getSinchServiceInterface().getAudioController();
//            audioController.enableSpeaker();
            if (call.getDetails().isVideoOffered()) {
                setVideoViewsVisibility(true, true);
            } else {
                setVideoViewsVisibility(false, false);
            }
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
        }

        @Override
        public void onCallEnded(Call call) {

            try {
                mDatabaseForCall.child("callEndingTime").setValue(String.valueOf(System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            }


            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            //Toast.makeText(CallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
            releaseWakeLock();
            endCall();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

    private class SinchCallListener implements VideoCallListener {

        @Override
        public void onCallEnded(Call call) {

            try {
                mDatabaseForCall.child("callEndingTime").setValue(String.valueOf(System.currentTimeMillis()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            //Toast.makeText(CallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
            releaseWakeLock();
            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");

            mDatabaseForCall.child("callStartingTime").setValue(String.valueOf(System.currentTimeMillis()));

            mAudioPlayer.stopProgressTone();
            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//            AudioController audioController = getSinchServiceInterface().getAudioController();
//            audioController.enableSpeaker();
            if (call.getDetails().isVideoOffered()) {
                setVideoViewsVisibility(true, true);
            }
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // No need to implement if you use managed push
        }

        @Override
        public void onVideoTrackAdded(Call call) {

        }

        @Override
        public void onVideoTrackPaused(Call call) {

        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }
    }

    /**
     * Acquires the wake lock
     */
    private void acquireWakeLock() {
        if (!wakeLock.isHeld()) {
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        }
    }
    /**
     * Releases the wake lock
     */
    private void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
