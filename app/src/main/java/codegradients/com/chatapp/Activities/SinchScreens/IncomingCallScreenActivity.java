package codegradients.com.chatapp.Activities.SinchScreens;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.video.VideoCallListener;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.AudioPlayer;
import codegradients.com.chatapp.helper_classes.SinchService;

public class IncomingCallScreenActivity extends BaseActivity {

    static final String TAG = IncomingCallScreenActivity.class.getSimpleName();
    private String mCallId;
    private AudioPlayer mAudioPlayer;
    private boolean mAcceptVideo = true;

    ImageView callTypeIcon;

    public static final String ACTION_ANSWER = "answer";
    public static final String ACTION_IGNORE = "ignore";
    public static final String EXTRA_ID = "id";
    public static int MESSAGE_ID = 14;
    private String mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.incoming);

        Button answer = (Button) findViewById(R.id.answerButton);
        answer.setOnClickListener(mClickListener);
        Button decline = (Button) findViewById(R.id.declineButton);
        decline.setOnClickListener(mClickListener);

        callTypeIcon = findViewById(R.id.callTypeIcon);

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();

        Intent intent = getIntent();
        mCallId = intent.getStringExtra(SinchService.CALL_ID);
        mAction = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(SinchService.CALL_ID) != null) {
                mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
            }
            final int id = intent.getIntExtra(EXTRA_ID, -1);
            if (id > 0) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(id);
            }
            mAction = intent.getAction();
        }
    }

    @Override
    protected void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
            TextView remoteUser = (TextView) findViewById(R.id.remoteUser);
            remoteUser.setText(getIntent().getStringExtra("callerName"));

            if (call.getDetails().isVideoOffered()) {
                callTypeIcon.setImageDrawable(getResources().getDrawable(R.drawable.call_video_icon_per));
            } else {
                callTypeIcon.setImageDrawable(getResources().getDrawable(R.drawable.call_audio_icon_per));
            }


            if (ACTION_ANSWER.equals(mAction)) {
                mAction = "";
                answerClicked();
            } else if (ACTION_IGNORE.equals(mAction)) {
                mAction = "";
                declineClicked();
            }

        } else {
            Log.e(TAG, "Started with invalid callId, aborting");
            finish();
        }
    }

    private void answerClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.answer();
            Intent intent = new Intent(this, CallScreenActivity.class);
            intent.putExtra("callerName", getIntent().getStringExtra("callerName"));
            intent.putExtra(SinchService.CALL_ID, mCallId);
            startActivity(intent);
        } else {
            finish();
        }
    }

    private void declineClicked() {
        mAudioPlayer.stopRingtone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private class SinchCallListener implements VideoCallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended, cause: " + cause.toString());
            mAudioPlayer.stopRingtone();
            finish();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // No need to implement for managed push
        }

        @Override
        public void onVideoTrackAdded(Call call) {
            // Display some kind of icon showing it's a video call
            // and pass it to the CallScreenActivity via Intent and mAcceptVideo
            mAcceptVideo = true;
        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }

        @Override
        public void onVideoTrackPaused(Call call) {

        }
    }

    private OnClickListener mClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.answerButton:
                    answerClicked();
                    break;
                case R.id.declineButton:
                    declineClicked();
                    break;
            }
        }
    };
}
