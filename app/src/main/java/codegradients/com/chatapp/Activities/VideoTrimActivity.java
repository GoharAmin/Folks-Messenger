package codegradients.com.chatapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.HelperClass;
import life.knowledge4.videotrimmer.K4LVideoTrimmer;
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;
import life.knowledge4.videotrimmer.utils.FileUtils;

public class VideoTrimActivity extends AppCompatActivity implements OnTrimVideoListener {
    @BindView(R.id.avi_video_trim)
    AVLoadingIndicatorView mAviVideoTrim;
    private K4LVideoTrimmer mVideoTrimmer;
    public static String KEY_URI = "uri_string";
    private static String TAG = VideoTrimActivity.class.getName();
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_trim);
        ButterKnife.bind(this);
        init();
        String uri = getIntent().getStringExtra(KEY_URI);
        mVideoTrimmer = findViewById(R.id.timeLine);
        Uri uri1 = Uri.parse(uri);
        String path = FileUtils.getPath(getApplicationContext(), uri1);
        mVideoTrimmer.setVideoURI(Uri.parse(path));
        mVideoTrimmer.setOnTrimVideoListener(this);
        mVideoTrimmer.setMaxDuration(30);
        uploading = 0;

        mAviVideoTrim = findViewById(R.id.avi_video_trim);

    }

    @Override
    public void getResult(Uri uri) {

        Uri u = Uri.fromFile(new File(uri.getPath()));
        long duration = HelperClass.getDurationOfVideo(u, getApplicationContext());

        Log.i("VideoTrimming__", "Video Duration____" + duration);

        uploadVideo(u, String.valueOf(duration));

    }

    int uploading = 0;

    private void uploadVideo(Uri uri, String duration) {
        try {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAviVideoTrim.setVisibility(View.VISIBLE);

                    if (uploading == 0){
                        uploading = 1;
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("status").child(mUserId);
                        String key = databaseReference.push().getKey();

                        StorageReference storage = FirebaseStorage.getInstance().getReference().child("status").child(mUserId).child(HelperClass.getFileNameFromUri(uri) + ".mp4");
                        storage.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                            storage.getDownloadUrl().addOnSuccessListener(uri1 -> {

                                HashMap hashMap = new HashMap();
                                hashMap.put("url", uri1.toString());
                                hashMap.put("status_upload_date", String.valueOf(System.currentTimeMillis()));
                                hashMap.put("status_type", "video");
                                hashMap.put("status_duration", duration);
                                databaseReference.child(key).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        finish();
                                    }
                                });


                            });

                        }).addOnSuccessListener(taskSnapshot -> {
                        }).addOnFailureListener(e -> {

                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


        private void navigateToActivity() {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

    }

    private void init() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    public void cancelAction() {

    }
}
