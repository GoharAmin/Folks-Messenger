package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shawnlin.numberpicker.NumberPicker;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.theartofdev.edmodo.cropper.CropImage;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.HashMap;

import codegradients.com.chatapp.Activities.AuthenticationScreens.LoginActivity;
import codegradients.com.chatapp.Activities.SinchScreens.BaseActivity;
import codegradients.com.chatapp.Activities.SinchScreens.CallScreenActivity;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.HelperClass;
import codegradients.com.chatapp.helper_classes.SinchService;

public class UserProfileActivity extends BaseActivity {

    boolean youBlocked = false, otherBlockedYou = false;

    TextView headingText;

    String userId = "";

    TextView userNameText, userNumberText, userAboutText;
    ImageView editNameImg, editNumberImg, editAboutImg, userImageImg, editImageImg;
    Button blockBtn, logoutBtn;

    Switch blockMediaSwitch, selfDestructMessageSwitch;

    public static SinchClient sinchClient;

    boolean otherBusyModeOn = false;

    boolean isSelfDestructMessageOn = false;
    String selfDestructTimeSelected = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userId = getIntent().getStringExtra("userId");

        initViews();

        getInfo();

        if (!userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            getBlockStatus();
        }
    }

    long selectedSelfDestructTimeInMillis = 0;

    private void initViews(){

        headingText = findViewById(R.id.detail_heading);

        findViewById(R.id.back_btn_user_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        logoutBtn = findViewById(R.id.logoutBtn);

        if (userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            logoutBtn.setVisibility(View.VISIBLE);
            headingText.setText("Profile");
        } else {
            logoutBtn.setVisibility(View.GONE);
        }

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setTitle("Loading");
        loadingDialog.setMessage("Loading Data. Please Wait");
        blockBtn = findViewById(R.id.blockUserBtn);
        userNameText = findViewById(R.id.userNameText);
        userNumberText = findViewById(R.id.userNumberText);
        userAboutText = findViewById(R.id.userAboutText);
        editNameImg = findViewById(R.id.editNameImg);
        editNumberImg = findViewById(R.id.editNumberImg);
        editAboutImg = findViewById(R.id.editAboutImg);
        userImageImg = findViewById(R.id.userImageImg);
        editImageImg = findViewById(R.id.editImageImg);
        blockMediaSwitch = findViewById(R.id.blockMediaSwitch);
        selfDestructMessageSwitch = findViewById(R.id.selfDestructMessageSwitch);

        Log.v("usermediaBlockCheck__", "Init Views");
        getMediaBlockByFriend();

        blockMediaSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                DatabaseReference mDatabaseForGettingInfo = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("mediaBlockedContacts");

                if (blockMediaSwitch.isChecked()) {
                    mDatabaseForGettingInfo.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                } else {
                    mDatabaseForGettingInfo.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                }
            }
        });

        selfDestructMessageSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference mDatabaseForSettingSelfDestructMessages = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("selfDestructMessage").child(userId);

                if (selfDestructMessageSwitch.isChecked()) {
                    showTimeForAutoDestructMessageChoosingDialog();
                } else {
                    mDatabaseForSettingSelfDestructMessages.child("status").setValue(false);
                    selfDestructMessageSwitch.setChecked(false);
                }
            }
        });

        if (userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            editNameImg.setVisibility(View.VISIBLE);
            editImageImg.setVisibility(View.VISIBLE);
            editAboutImg.setVisibility(View.VISIBLE);
            editNumberImg.setVisibility(View.GONE);
            blockBtn.setVisibility(View.GONE);
        } else {
            editNameImg.setVisibility(View.GONE);
            editImageImg.setVisibility(View.GONE);
            editAboutImg.setVisibility(View.GONE);
            editNumberImg.setVisibility(View.GONE);
            blockBtn.setVisibility(View.VISIBLE);

            userImageImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(UserProfileActivity.this, ImageViewingActivity.class).putExtra("URL", olderPicture));
                }
            });
        }

        editImageImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(UserProfileActivity.this);
            }
        });

        editNameImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeInfo("Change Name", "username", userNameText.getText().toString());
            }
        });

        editAboutImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeInfo("Change About", "about", userAboutText.getText().toString());
            }
        });

        blockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                blockBtn.setEnabled(false);

                if (youBlocked) {
                    DatabaseReference mDatabaseForBlockingUser = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("BlockedUsers");
                    mDatabaseForBlockingUser.child(userId).setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                blockBtn.setEnabled(true);
                                Toast.makeText(UserProfileActivity.this, "Unblocked User", Toast.LENGTH_SHORT).show();
                            } else {
                                blockBtn.setEnabled(true);
                                Toast.makeText(UserProfileActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    DatabaseReference mDatabaseForBlockingUser = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("BlockedUsers");
                    mDatabaseForBlockingUser.child(userId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                blockBtn.setEnabled(true);
                                Toast.makeText(UserProfileActivity.this, "Blocked User", Toast.LENGTH_SHORT).show();
                            } else {
                                blockBtn.setEnabled(true);
                                Toast.makeText(UserProfileActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        if (userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            findViewById(R.id.callIcon).setVisibility(View.GONE);
            findViewById(R.id.videoIcon).setVisibility(View.GONE);
        } else {
            findViewById(R.id.callIcon).setVisibility(View.VISIBLE);
            findViewById(R.id.videoIcon).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.videoIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable()){
                    Toast.makeText(UserProfileActivity.this, "Please Connect to an active Connection in order to make a call", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (otherBusyModeOn) {
                    MDToast.makeText(UserProfileActivity.this, "Your Friend Is Busy At This Moment", MDToast.LENGTH_LONG, MDToast.TYPE_INFO).show();
                    return;
                }

                getSinchServiceInterface().startClient();

                Call call = getSinchServiceInterface().callUserVideo(userId);
                String callId = call.getCallId();

                Intent callScreen = new Intent(UserProfileActivity.this, CallScreenActivity.class);
                callScreen.putExtra(SinchService.CALL_ID, callId);
                callScreen.putExtra("callerName", userId);
                startActivity(callScreen);

                HashMap hashMapData = new HashMap();

                hashMapData.put("callerId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMapData.put("calleeId", userId);
                hashMapData.put("callPlacingTime", String.valueOf(System.currentTimeMillis()));
                hashMapData.put("isVideo", call.getDetails().isVideoOffered());

                DatabaseReference mDatabaseForCall = FirebaseDatabase.getInstance().getReference().child("callsRecords").child(callId);
                mDatabaseForCall.updateChildren(hashMapData).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {

                    }
                });

            }
        });

        findViewById(R.id.callIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isNetworkAvailable()){
                    Toast.makeText(UserProfileActivity.this, "Please Connect to an active Connection in order to make a call", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (otherBusyModeOn) {
                    MDToast.makeText(UserProfileActivity.this, "Your Friend Is Busy At This Moment", MDToast.LENGTH_LONG, MDToast.TYPE_INFO).show();
                    return;
                }

                getSinchServiceInterface().startClient();

                Call call = getSinchServiceInterface().callUserAudio(userId);
                String callId = call.getCallId();

                Intent callScreen = new Intent(UserProfileActivity.this, CallScreenActivity.class);
                callScreen.putExtra(SinchService.CALL_ID, callId);
                callScreen.putExtra("callerName", userId);
                startActivity(callScreen);

                HashMap hashMapData = new HashMap();

                hashMapData.put("callerId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMapData.put("calleeId", userId);
                hashMapData.put("callPlacingTime", String.valueOf(System.currentTimeMillis()));
                hashMapData.put("isVideo", call.getDetails().isVideoOffered());

                DatabaseReference mDatabaseForCall = FirebaseDatabase.getInstance().getReference().child("callsRecords").child(callId);
                mDatabaseForCall.updateChildren(hashMapData).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {

                    }
                });
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
                finishAndRemoveTask();
            }
        });
    }

    private void getMediaBlockByFriend() {
        Log.v("usermediaBlockCheck__", "Before Listener");
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("mediaBlockedContacts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.v("usermediaBlockCheck__", "DataSnapShot Exists");

                            if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(Boolean.class)) {
                                    Log.v("usermediaBlockCheck__", "Media Blocked");
                                    blockMediaSwitch.setChecked(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(Boolean.class));
                                    //isMediaBlockedByOtherPerson = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(Boolean.class);
                                } else {
                                    Log.v("usermediaBlockCheck__", "Media is not Blocked");
                                    blockMediaSwitch.setChecked(false);
                                }
                            } else {
                                Log.v("usermediaBlockCheck__", "My ID is not Media is not Blocked");
                                blockMediaSwitch.setChecked(false);
                            }
                        } else {
                            Log.v("usermediaBlockCheck__", "DataSnapShot Does Not Exists");
                            blockMediaSwitch.setChecked(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void showTimeForAutoDestructMessageChoosingDialog() {
        DatabaseReference mDatabaseForSettingSelfDestructMessages = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("selfDestructMessage").child(userId);

        AlertDialog.Builder builder1 = new AlertDialog.Builder(UserProfileActivity.this);
        //setting up the layout for alert dialog
        View view1 = LayoutInflater.from(UserProfileActivity.this).inflate(R.layout.dialog_self_destruct_message_layout, null, false);

        builder1.setView(view1);

        AlertDialog ratingDialog = builder1.create();
        ratingDialog.show();

        selectedSelfDestructTimeInMillis = Long.parseLong(selfDestructTimeSelected);

        int selectedPosition = 1;

        switch (selfDestructTimeSelected){
            case "1000":
                selectedPosition = 1;
                break;
            case "2000":
                selectedPosition = 2;
                break;
            case "3000":
                selectedPosition = 3;
                break;
            case "4000":
                selectedPosition = 4;
                break;
            case "5000":
                selectedPosition = 5;
                break;
            case "10000":
                selectedPosition = 6;
                break;
            case "20000":
                selectedPosition = 7;
                break;
            case "30000":
                selectedPosition = 8;
                break;
            case "40000":
                selectedPosition = 9;
                break;
            case "50000":
                selectedPosition = 10;
                break;
            case "60000":
                selectedPosition = 11;
                break;
            case "300000":
                selectedPosition = 12;
                break;
            case "1800000":
                selectedPosition = 13;
                break;
            case "3600000":
                selectedPosition = 14;
                break;
            case "86400000":
                selectedPosition = 15;
                break;
            case "2629746000":
                selectedPosition = 16;
                break;
            case "31556952000":
                selectedPosition = 17;
                break;
        }

        //Toast.makeText(UserProfileActivity.this, "Pos: " + selectedPosition, Toast.LENGTH_SHORT).show();

        NumberPicker numberPicker = view1.findViewById(R.id.number_picker);
        Button submitBtn = view1.findViewById(R.id.submitBtn);
        String[] data = {"1 seconds", "2 seconds", "3 seconds", "4 seconds", "5 seconds", "10 seconds", "20 seconds", "30 seconds", "40 seconds", "50 seconds", "1 minute", "5 minutes", "30 minutes", "1 hour", "1 day", "1 week", "1 Month"};
        numberPicker.setTextColor(Color.parseColor("#993B61F6"));
        numberPicker.setSelectedTextColor(getResources().getColor(R.color.colorPrimary));
        numberPicker.setMaxValue(data.length);
        numberPicker.setDisplayedValues(data);
        numberPicker.setScrollerEnabled(true);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setFadingEdgeEnabled(true);
        numberPicker.setValue(selectedPosition);
        numberPicker.setMinValue(1);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (numberPicker.getValue()){
                    case 1:
                        selectedSelfDestructTimeInMillis = 1000;
                        break;
                    case 2:
                        selectedSelfDestructTimeInMillis = 2000;
                        break;
                    case 3:
                        selectedSelfDestructTimeInMillis = 3000;
                        break;
                    case 4:
                        selectedSelfDestructTimeInMillis = 4000;
                        break;
                    case 5:
                        selectedSelfDestructTimeInMillis = 5000;
                        break;
                    case 6:
                        selectedSelfDestructTimeInMillis = 10000;
                        break;
                    case 7:
                        selectedSelfDestructTimeInMillis = 20000;
                        break;
                    case 8:
                        selectedSelfDestructTimeInMillis = 30000;
                        break;
                    case 9:
                        selectedSelfDestructTimeInMillis = 40000;
                        break;
                    case 10:
                        selectedSelfDestructTimeInMillis = 50000;
                        break;
                    case 11:
                        selectedSelfDestructTimeInMillis = 60000;
                        break;
                    case 12:
                        selectedSelfDestructTimeInMillis = 300000;
                        break;
                    case 13:
                        selectedSelfDestructTimeInMillis = 1800000;
                        break;
                    case 14:
                        selectedSelfDestructTimeInMillis = 3600000;
                        break;
                    case 15:
                        selectedSelfDestructTimeInMillis = 86400000;
                        break;
                    case 16:
                        selectedSelfDestructTimeInMillis = 2629746000L;
                        break;
                    case 17:
                        selectedSelfDestructTimeInMillis = 31556952000L;
                        break;
                }

                mDatabaseForSettingSelfDestructMessages.child("status").setValue(true);
                mDatabaseForSettingSelfDestructMessages.child("time").setValue(String.valueOf(selectedSelfDestructTimeInMillis));

                ratingDialog.dismiss();
            }
        });

        ratingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                selfDestructMessageSwitch.setChecked(false);
            }
        });
    }

    private Call call;

    private void addListener() {
//        Intent intent = new Intent(UserProfileActivity.this, OutgoingCallActivity.class);
//        intent.putExtra("call_id", call.getCallId());
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }

    private void changeInfo(String heading, final String change, String previousData) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(UserProfileActivity.this);
        //setting up the layout for alert dialog
        View view1 = LayoutInflater.from(UserProfileActivity.this).inflate(R.layout.change_user_info_alert_dialog, null, false);

        builder1.setView(view1);

        final TextView headingTextView = view1.findViewById(R.id.heading_edit_user_detail_detail);
        headingTextView.setText(heading);

        final EditText editingEditText = view1.findViewById(R.id.change_edit_user_detail_dialog);
        editingEditText.setText(previousData);
        editingEditText.setSelection(editingEditText.getText().length());
        // Set up the buttons
        builder1.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                if (editingEditText.getText().toString().isEmpty()) {
                    Toast.makeText(UserProfileActivity.this, "Please fill the field above first", Toast.LENGTH_SHORT).show();
                } else {
                    if (change.equals("username")){

                        DatabaseReference mDatabaseForChaning = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        mDatabaseForChaning.child("userName").setValue(HelperClass.encrypt(editingEditText.getText().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                MDToast.makeText(UserProfileActivity.this, "Name Changed Successfully", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
                                dialog.dismiss();
                            }
                        });

                    } else if (change.equals("about")){
                        DatabaseReference mDatabaseForChaning = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        mDatabaseForChaning.child("about").setValue(HelperClass.encrypt(editingEditText.getText().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                MDToast.makeText(UserProfileActivity.this, "Status Changed Successfully", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
                                dialog.dismiss();
                            }
                        });
                    }

                }
            }
        });
        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder1.show();
    }



    private void getInfo(){
        DatabaseReference mDatabaseForGettingInfo = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        mDatabaseForGettingInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("busyMode")) {
                    if (dataSnapshot.child("busyMode").hasChild("status")) {
                        otherBusyModeOn = dataSnapshot.child("busyMode").child("status").getValue(Boolean.class);
                    }
                }

                if (dataSnapshot.hasChild("userName")){
                    String username = HelperClass.decrypt(dataSnapshot.child("userName").getValue(String.class));

                    //Toast.makeText(UserProfileActivity.this, "Name: " + username, Toast.LENGTH_SHORT).show();
                    userNameText.setText(username);
                    headingText.setText(username);
                }

                if (dataSnapshot.hasChild("about")){
                    String about = HelperClass.decrypt(dataSnapshot.child("about").getValue(String.class));
                    userAboutText.setText(about);
                }

                if (dataSnapshot.hasChild("mobileNumber")){
                    String number = HelperClass.decrypt(dataSnapshot.child("mobileNumber").getValue(String.class));
                    userNumberText.setText(number);
                }

                if (dataSnapshot.hasChild("profileImage")){
                    String profileImage = HelperClass.decrypt(dataSnapshot.child("profileImage").getValue(String.class));

                    olderPicture = profileImage;

                    try {
                        Glide.with(UserProfileActivity.this).load(profileImage).into(userImageImg);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference mDatabaseForGettingMyOwnData = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseForGettingMyOwnData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("selfDestructMessage")) {

                    if (dataSnapshot.child("selfDestructMessage").hasChild(userId)) {
                        try {
                            isSelfDestructMessageOn = dataSnapshot.child("selfDestructMessage").child(userId).child("status").getValue(Boolean.class);

                            selfDestructMessageSwitch.setChecked(isSelfDestructMessageOn);

                            selfDestructTimeSelected = dataSnapshot.child("selfDestructMessage").child(userId).child("time").getValue(String.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    String olderPicture = "";

    Uri imageUri;

    ProgressDialog loadingDialog;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingDialog.setTitle("Uploading");
                loadingDialog.setMessage("Uploading Image. Please Wait");
                loadingDialog.show();
                imageUri = result.getUri();

                uploadImage();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadImage(){

        StorageReference storage = FirebaseStorage.getInstance().getReference().child("userImages").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        storage.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storage.getDownloadUrl().addOnSuccessListener(uri ->{

                String imageLink = String.valueOf(uri);

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                mDatabase.child("profileImage").setValue(HelperClass.encrypt(imageLink)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserProfileActivity.this, "Image Successfully Uploaded", Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

            });

        }).addOnProgressListener(taskSnapshot -> {

        }).addOnFailureListener(e ->{

            loadingDialog.dismiss();
            Toast.makeText(this, "Sorry Image was not uploaded: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void getBlockStatus(){
        DatabaseReference mDatabaseToGetStatus = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseToGetStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("BlockedUsers")) {
                    if (dataSnapshot.child("BlockedUsers").hasChild(userId)) {
                        Boolean Status = dataSnapshot.child("BlockedUsers").child(userId).getValue(Boolean.class);
                        try {
                            if (Status) {
                                youBlocked = true;
                            } else {
                                youBlocked = false;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        youBlocked = false;
                    }
                } else {
                    youBlocked = false;
                }

                if (youBlocked){
                    blockBtn.setText("Unblock");
                } else {
                    blockBtn.setText("Block");
                }

                DatabaseReference mDatabaseToGetOtherBlockStatus = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                mDatabaseToGetOtherBlockStatus.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshott) {
                        if (dataSnapshott.exists()) {
                            if (dataSnapshott.hasChild("BlockedUsers")) {
                                if (dataSnapshott.child("BlockedUsers").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    try {
                                        Boolean Status = dataSnapshott.child("BlockedUsers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(Boolean.class);
                                        if (Status) {
                                            otherBlockedYou = true;
                                        } else {
                                            otherBlockedYou = false;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    otherBlockedYou = false;
                                }
                            } else {
                                otherBlockedYou = false;
                            }
                        }

                        if (!youBlocked && !otherBlockedYou){
                            findViewById(R.id.callIcon).setEnabled(true);
                        } else {

                            findViewById(R.id.callIcon).setEnabled(false);
                            if (youBlocked){
                                Toast.makeText(UserProfileActivity.this, "You have blocked this person. So you cannot place a Call with this user.", Toast.LENGTH_LONG).show();
                            }

                            if (otherBlockedYou){
                                Toast.makeText(UserProfileActivity.this, "This person have blocked you. So you cannot place a Call with this user.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean isNetworkAvailable() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
