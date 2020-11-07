package codegradients.com.chatapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.valdesekamdem.library.mdtoast.MDToast;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import codegradients.com.chatapp.R;

public class RecordAudioActivity extends AppCompatActivity implements View.OnClickListener {

    TextView mTextViewTime;
    ImageView mTextViewRecord;
    ImageView mTextViewPlay;
    ImageView mTextViewStop;
    CardView mCardViewSave;
    private MediaRecorder mediaRecorder;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    Handler handler;
    int Seconds, Minutes, MilliSeconds;

    String AudioSavePathInDevice = null;
    Random random;
    MediaPlayer mediaPlayer;

    public static final int RequestPermissionCode = 1;
    String audioUrl = "";
    public static final String KEY_ACTIVITY_TYPE = "activity_type";
    private String mActivityType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);

        mTextViewTime = findViewById(R.id.textView_Time);
        mTextViewRecord = findViewById(R.id.textView_Record);
        mTextViewPlay = findViewById(R.id.textView_Play);
        mTextViewPlay.setVisibility(View.GONE);
        mTextViewStop = findViewById(R.id.textView_Stop);
        mCardViewSave = findViewById(R.id.cardView_Save);

        settingListener();
        gettingIntent();
        handler = new Handler();
        random = new Random();

        mCardViewSave.setClickable(false);
        mTextViewPlay.setClickable(false);
        mTextViewStop.setClickable(false);
        mTextViewRecord.setClickable(true);
    }

    private void gettingIntent() {
        mActivityType = getIntent().getStringExtra(KEY_ACTIVITY_TYPE);
    }

    private void settingListener() {
        mCardViewSave.setOnClickListener(this);
        mTextViewRecord.setOnClickListener(this);
        mTextViewPlay.setOnClickListener(this);
        mTextViewStop.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == mTextViewRecord.getId()) {
            startRecording();
        } else if (id == mTextViewStop.getId()) {
            stopRecording();
        } else if (id == mCardViewSave.getId()) {
            saveAudio();
        } else if (id == mTextViewPlay.getId()) {
            playRecording();
        }
    }

    private void saveAudio() {

        try {
            mediaRecorder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Recording Completed", Toast.LENGTH_SHORT).show();
        TimeBuff += MillisecondTime;
        handler.removeCallbacks(runnable);
        mCardViewSave.setClickable(true);
        mTextViewPlay.setClickable(true);
        mTextViewStop.setClickable(false);
        mTextViewRecord.setClickable(true);

        MillisecondTime = 0L;
        StartTime = 0L;
        TimeBuff = 0L;
        UpdateTime = 0L;
        Seconds = 0;
        Minutes = 0;
        MilliSeconds = 0;
        mTextViewTime.setText("00:00:00");
        Log.v("AudioUpload", "Save Button Pressed");

        Toast.makeText(this, "Audio Saved Successfully", Toast.LENGTH_SHORT).show();
        if (mActivityType.equals("gc")) {
            Intent intent = new Intent(this, GroupChatActivity.class);
            intent.putExtra("savePath", AudioSavePathInDevice);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else if (mActivityType.equals("ch")) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("savePath", AudioSavePathInDevice);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    public void MediaRecorderReady() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setMaxDuration(300000);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            mTextViewTime.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            handler.postDelayed(this, 0);
        }

    };


    private void startRecording() {

        MillisecondTime = 0L;
        StartTime = 0L;
        TimeBuff = 0L;
        UpdateTime = 0L;
        Seconds = 0;
        Minutes = 0;
        MilliSeconds = 0;
        mTextViewTime.setText("00:00:00");

        String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/ChatAppRecordings";

        File folderPathFile = new File(folderPath);

        if (!folderPathFile.exists()) {
            folderPathFile.mkdirs();
        }


        AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/ChatAppRecordings/" + Calendar.getInstance().getTime() + "AudioRecording.mp3";
        MediaRecorderReady();
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            StartTime = SystemClock.uptimeMillis();
            handler.postDelayed(runnable, 0);
            mCardViewSave.setClickable(true);
            mTextViewPlay.setClickable(false);
            mTextViewPlay.setVisibility(View.GONE);
            mTextViewStop.setClickable(true);
            mTextViewStop.setVisibility(View.VISIBLE);
            mTextViewRecord.setVisibility(View.GONE);
            mTextViewRecord.setClickable(false);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Toast.makeText(this, "Recording started",
                Toast.LENGTH_SHORT).show();

    }

    public void stopRecording() {
        mediaRecorder.stop();
        MDToast.makeText(RecordAudioActivity.this, "Recording Stopped", MDToast.LENGTH_SHORT, MDToast.TYPE_INFO).show();
        TimeBuff += MillisecondTime;
        handler.removeCallbacks(runnable);
        mCardViewSave.setClickable(true);
        mTextViewPlay.setClickable(true);
        mTextViewRecord.setImageDrawable(getResources().getDrawable(R.drawable.aar_ic_restart));
        mTextViewStop.setClickable(false);
        mTextViewStop.setVisibility(View.GONE);
        mTextViewRecord.setVisibility(View.VISIBLE);
        mTextViewPlay.setVisibility(View.VISIBLE);
        mTextViewRecord.setClickable(true);

        try {
            mediaPlayer = null;
        } catch (Exception e) {

        }
    }

    public void playRecording() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mTextViewPlay.setImageDrawable(getResources().getDrawable(R.drawable.aar_ic_play));
            } else {
                mTextViewPlay.setImageDrawable(getResources().getDrawable(R.drawable.aar_ic_pause));
                mediaPlayer.start();
            }
        } else {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(AudioSavePathInDevice);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mTextViewPlay.setImageDrawable(getResources().getDrawable(R.drawable.aar_ic_pause));

            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mTextViewPlay.setImageDrawable(getResources().getDrawable(R.drawable.aar_ic_play));
                }
            });
            //MDToast.makeText(RecordAudioActivity.this, "Recording Playing", MDToast.LENGTH_SHORT, MDToast.TYPE_INFO).show();
        }

    }
}

