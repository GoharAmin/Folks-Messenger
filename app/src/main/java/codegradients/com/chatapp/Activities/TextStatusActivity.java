package codegradients.com.chatapp.Activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.HelperClass;

public class TextStatusActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.editTxt_status)
    EditText mEditTxtStatus;
    @BindView(R.id.btn_send)
    FloatingActionButton mBtnSend;
    @BindView(R.id.linrLayout_status)
    LinearLayout mLinrLayoutStatus;
    private static String TAG = TextStatusActivity.class.getName();
    String mUserId;
    @BindView(R.id.avi_text_status)
    AVLoadingIndicatorView mAviTextStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_status);
        ButterKnife.bind(this);
        init();
        settingListener();
    }

    private void init() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    private void settingListener() {
        mBtnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == mBtnSend.getId()) {
            if (!mEditTxtStatus.getText().equals("")) {
                Bitmap bitmap = getBitmapFromView(mLinrLayoutStatus);
                Log.i(TAG, "" + bitmap);
                Uri uri = HelperClass.saveImageToInternalStorage(getApplicationContext(), bitmap);
                uploadImage(uri);
            } else {
                HelperClass.createToast(getApplicationContext(), "Please write some thing first");
            }

        }
    }

    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        mEditTxtStatus.setCursorVisible(false);
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }


    private void uploadImage(Uri uri) {
        mAviTextStatus.setVisibility(View.VISIBLE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("status").child(mUserId);
        String key = databaseReference.push().getKey();

        StorageReference storage = FirebaseStorage.getInstance().getReference().child("status").child(mUserId).child(HelperClass.getFileNameFromUri(uri));
        storage.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            storage.getDownloadUrl().addOnSuccessListener(uri1 -> {
                HashMap hashMap = new HashMap();
                hashMap.put("url", uri1.toString());
                // Dat Calendar.getInstance().getTime();
                hashMap.put("status_upload_date", String.valueOf(System.currentTimeMillis()));
                hashMap.put("status_type", "image");
                hashMap.put("status_duration", "5000");
                databaseReference.child(key).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mAviTextStatus.setVisibility(View.INVISIBLE);
                        Toast.makeText(TextStatusActivity.this, "Text Status Uploaded", Toast.LENGTH_SHORT).show();
                        navigateToActivity();
                    }
                });


            });

        }).addOnSuccessListener(taskSnapshot -> {
        }).addOnFailureListener(e -> {
            mAviTextStatus.setVisibility(View.INVISIBLE);
            Log.i(TAG, "" + e.getMessage());
            HelperClass.createToast(getApplicationContext(), "" + e.getMessage());
        });

    }

    private void navigateToActivity() {
        finish();
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        finish();

    }
}
