package codegradients.com.chatapp.helper_classes;

import android.media.AudioManager;
import android.media.MediaPlayer;


public class PlayerTwo {
    private static PlayerTwo playerTwo;
    public MediaPlayer mMediaPlayer;
    public String streamUrl;
    public int streamRes;
    public synchronized static PlayerTwo shared() {
        if (playerTwo == null) {
            playerTwo = new PlayerTwo();
        }
        return playerTwo;
    }
    public void setupPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setVolume(1f, 1f);
    }
    public void playMedia(String url) {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            mMediaPlayer.setDataSource(url);
            streamUrl = url;
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
//                    if (listener != null) {
//                        listener.onMediaStarted();
//                    }
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
//                    if (listener != null) {
//                        listener.onMediaStopped();
//                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}