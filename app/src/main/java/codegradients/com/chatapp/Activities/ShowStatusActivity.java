package codegradients.com.chatapp.Activities;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import codegradients.com.chatapp.Models.StatusInfoModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.StoriesProgressView.StoriesProgressView;


public class ShowStatusActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {
    public static final String KEY_LIST = "arraylist_key";
    public static final String KEY_STATUS_TYPE = "status_type";
    @BindView(R.id.imgview_staus)
    ImageView mImgviewStaus;
    @BindView(R.id.reverse)
    View mReverse;
    @BindView(R.id.skip)
    View mSkip;
    @BindView(R.id.stories)
    StoriesProgressView mStories;
    private ArrayList<StatusInfoModel> mInfoModelsList = new ArrayList<>();
    private static String TAG = ShowStatusActivity.class.getName();
    private int counter = 0;
    long pressTime = 0L;
    long limit = 500L;
    private boolean playWhenReady = true;
    private String mStatusType;
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    mStories.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    mStories.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };
    @BindView(R.id.playerView_status)
     PlayerView mPlayerView;
    private SimpleExoPlayer exoPlayer;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_show_status);
        ButterKnife.bind(this);
        gettingIntent();
        setStoriesProgressView();

    }

    private void setStoriesProgressView() {
        mStories.setStoriesCount(mInfoModelsList.size());
        long [] durations = new long[mInfoModelsList.size()];
        for (int i=0; i<mInfoModelsList.size(); i++){
            durations[i] = Long.parseLong(mInfoModelsList.get(i).getDuration());
        }
        mStories.setStoriesCountWithDurations(durations);
        mStories.setStoriesListener(this);


        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exoPlayer != null){
                    exoPlayer.release();
                    exoPlayer = null;
                }

                mStories.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        // bind skip view
        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (exoPlayer != null){
                    exoPlayer.release();
                    exoPlayer = null;
                }

                mStories.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);
        mStories.startStories(counter);

        if (mInfoModelsList.get(counter).getStatusType().equals("image")) {
            mPlayerView.setVisibility(View.GONE);
            mImgviewStaus.setVisibility(View.VISIBLE);
            Glide.with(this).load(mInfoModelsList.get(counter).getUrl()).into(mImgviewStaus);
        } else {
            mStories.pause();
            mImgviewStaus.setVisibility(View.GONE);
            mPlayerView.setVisibility(View.VISIBLE);
            initializePlayer(mInfoModelsList.get(counter).getUrl());
        }
    }

    private void gettingIntent() {
        mInfoModelsList = (ArrayList<StatusInfoModel>) getIntent().getSerializableExtra(KEY_LIST);
        mStatusType = getIntent().getStringExtra(KEY_STATUS_TYPE);
        Log.i(TAG, "arraylist size__________" + mInfoModelsList.size());
    }

    @Override
    public void onNext() {
        if (mStatusType.equals("other")) {
            updateView(mInfoModelsList.get(counter).getStatusId(), mInfoModelsList.get(counter).getUserId());
        }

        counter = counter+1;
        Log.i(TAG,"Counter in Next_________"+counter);
        if (counter<mInfoModelsList.size()) {
            if (mInfoModelsList.get(counter).getStatusType().equals("image")) {
                releasePlayer();
                mPlayerView.setVisibility(View.GONE);
                mImgviewStaus.setVisibility(View.VISIBLE);
                //mStories.pause();
                Glide.with(this).load(mInfoModelsList.get(counter).getUrl()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {

                        //mStories.resume();
                        return false;
                    }
                }).into(mImgviewStaus);
                if (mStatusType.equals("other")) {
                    updateView(mInfoModelsList.get(counter).getStatusId(), mInfoModelsList.get(counter).getUserId());
                }
            } else {
                mImgviewStaus.setVisibility(View.GONE);
                mPlayerView.setVisibility(View.VISIBLE);
                releasePlayer();
                initializePlayer(mInfoModelsList.get(counter).getUrl());
                if (mStatusType.equals("other")) {
                    updateView(mInfoModelsList.get(counter).getStatusId(), mInfoModelsList.get(counter).getUserId());
                }
            }
        }
    }

    private void updateView(String statusId,String userId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("status").child(userId).child(statusId);
        databaseReference.child("views").child(FirebaseAuth.getInstance().getUid()).setValue(String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void onPrev() {
         counter = counter-1;
         Log.i(TAG,"Counter in prev_________"+counter);
        if ((counter) < 0) return;
        if (mInfoModelsList.get(counter).getStatusType().equals("image")) {
            releasePlayer();
            mPlayerView.setVisibility(View.GONE);
            mImgviewStaus.setVisibility(View.VISIBLE);
            Glide.with(this).load(mInfoModelsList.get(counter).getUrl()).into(mImgviewStaus);

        } else {
            mImgviewStaus.setVisibility(View.GONE);
            mPlayerView.setVisibility(View.VISIBLE);
            releasePlayer();
            initializePlayer(mInfoModelsList.get(counter).getUrl());

        }

        Log.i(TAG,"LInks_______Counter____"+mInfoModelsList.get(counter).getUrl());
    }

    @Override
    public void onComplete() {
        Log.i(TAG,"counter in onComplete"+counter);
//        updateView(mInfoModelsList.get(counter).getStatusId(),mInfoModelsList.get(counter).getUserId());

        releasePlayer();
        finish();
    }
    private void hideSystemUi() {
        mPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void initializePlayer(String url) {

        Log.v("HereStatus__", "Hre start Initialize");
        mStories.pause();

        Log.v("HereStatus__", "Paused");

        exoPlayer = ExoPlayerFactory.newSimpleInstance(this);
        mPlayerView.setPlayer(exoPlayer);
        Uri uri = Uri.parse(url);
        MediaSource mediaSource = buildMediaSource(uri);
        exoPlayer.setPlayWhenReady(playWhenReady);
        exoPlayer.seekTo(currentWindow, playbackPosition);
        exoPlayer.prepare(mediaSource, false, false);

        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mStories.resume();
                        }
                    });

                    Log.v("HereStatus__", "Init Done");
                    //Toast.makeText(ShowStatusActivity.this, "Playing", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, "exoplayer-codelab");
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            playWhenReady = exoPlayer.getPlayWhenReady();
            playbackPosition = exoPlayer.getCurrentPosition();
            currentWindow = exoPlayer.getCurrentWindowIndex();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}
