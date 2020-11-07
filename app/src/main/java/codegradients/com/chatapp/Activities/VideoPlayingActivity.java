package codegradients.com.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;
import fm.jiecao.jcvideoplayer_lib.JCVideoPlayerStandard;
import codegradients.com.chatapp.R;

public class VideoPlayingActivity extends AppCompatActivity {

    String senderName = "";
    String videoUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_playing);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.blackColor, this.getTheme()));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.blackColor));
        }

        senderName = getIntent().getStringExtra("headingText");
        videoUrl = getIntent().getStringExtra("videoURL");

        //Toast.makeText(this, "URL: " + videoUrl, Toast.LENGTH_SHORT).show();

        initViews();
    }

    private void initViews(){
        findViewById(R.id.back_btn_).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView senderNameText = findViewById(R.id.userNameChat);
        senderNameText.setText(senderName);

        JCVideoPlayerStandard jcVideoPlayerStandard = (JCVideoPlayerStandard) findViewById(R.id.videoplayer);
        jcVideoPlayerStandard.setUp(videoUrl
                , JCVideoPlayerStandard.SCREEN_LAYOUT_NORMAL, "");
//        jcVideoPlayerStandard.thumbImageView.setImage("http://p.qpic.cn/videoyun/0/2449_43b6f696980311e59ed467f22794e792_1/640");

        jcVideoPlayerStandard.battery_level.setVisibility(View.GONE);

//        jcVideoPlayerStandard.bottomProgressBar.setVisibility(View.GONE);

        Glide.with(VideoPlayingActivity.this).load(videoUrl).into(jcVideoPlayerStandard.thumbImageView);
    }

    @Override
    public void onBackPressed() {
        if (JCVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }
    @Override
    protected void onPause() {
        super.onPause();
        JCVideoPlayer.releaseAllVideos();
    }
}
