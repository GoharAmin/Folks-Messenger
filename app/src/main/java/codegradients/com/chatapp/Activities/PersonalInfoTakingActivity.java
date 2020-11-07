package codegradients.com.chatapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;

import butterknife.ButterKnife;
import codegradients.com.chatapp.helper_classes.HelperClass;
import de.hdodenhof.circleimageview.CircleImageView;
import codegradients.com.chatapp.Activities.AuthenticationScreens.LoginActivity;
import codegradients.com.chatapp.R;

public class PersonalInfoTakingActivity extends AppCompatActivity {

    CircleImageView mImgViewProfile;
    EditText mEditTxtName;
    EditText mEditTextAbout;
    Button mButtonNext;

    AVLoadingIndicatorView avi;
    private Uri imageUri;

    String mUserID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info_taking);
        ButterKnife.bind(this);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        initViews();
    }

    private void initViews(){
        avi = findViewById(R.id.avi);
        avi.setVisibility(View.GONE);
        mEditTxtName = findViewById(R.id.editTxt_name);
        mEditTextAbout = findViewById(R.id.editTXt_about);

        mImgViewProfile = findViewById(R.id.imgView_profile);

        mImgViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(PersonalInfoTakingActivity.this);
            }
        });
        mButtonNext = findViewById(R.id.btn_next);
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEditTxtName.getText().toString().isEmpty()){
                    mEditTxtName.setError("Please Enter Your Name First");
                    return;
                }

                if (mEditTextAbout.getText().toString().isEmpty()){
                    mEditTextAbout.setError("Please Enter Something about you");
                    return;
                }

                if (picEntered == 0){
                    Toast.makeText(PersonalInfoTakingActivity.this, "Please Select your Photo", Toast.LENGTH_SHORT).show();
                    return;
                }

                avi.setVisibility(View.VISIBLE);
                mButtonNext.setEnabled(false);
                uploadData();
            }
        });
    }

    int picEntered = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                picEntered = 1;
                imageUri = result.getUri();
                mImgViewProfile.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadData() {
        String name = mEditTxtName.getText().toString();
        String about = mEditTextAbout.getText().toString();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        StorageReference storage = FirebaseStorage.getInstance().getReference().child("userImages").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        storage.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storage.getDownloadUrl().addOnSuccessListener(uri ->{

                HashMap hashMap = new HashMap();
                hashMap.put("userName", HelperClass.encrypt(name));
                hashMap.put("about", HelperClass.encrypt(about));
                hashMap.put("profileImage",HelperClass.encrypt(String.valueOf(uri)));
                hashMap.put("mobileNumber",HelperClass.encrypt(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()));
                databaseReference.child(mUserID).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        avi.setVisibility(View.GONE);
                        mButtonNext.setEnabled(true);
                       // uploadDisplayName(name);
                        startActivity(new Intent(PersonalInfoTakingActivity.this, MainActivity.class));
                        try {
                            LoginActivity.getLoginActivity().finish();
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                        finish();
                    }
                });

            });

        }).addOnProgressListener(taskSnapshot -> {

        }).addOnFailureListener(e ->{

            avi.setVisibility(View.GONE);
            mButtonNext.setEnabled(true);
            Toast.makeText(this, "Sorry Image was not uploaded: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void uploadDisplayName(String name) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                    }
                });
    }
}

