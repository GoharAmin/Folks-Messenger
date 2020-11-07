package codegradients.com.chatapp.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.valdesekamdem.library.mdtoast.MDToast;

import codegradients.com.chatapp.Activities.ImageViewingActivity;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.HelperClass;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserProfileFragment extends Fragment {

    TextView userNameText, userNumberText, userAboutText;
    ImageView editNameImg, editNumberImg, editAboutImg, userImageImg, editImageImg;
    Switch notifyGroupLeaving, busyModeSwitch, autoReplySwitch, screenshotPreventionSwitch, lastSeenSwitch;

    String autoReplyMessage = "";

    public UserProfileFragment() {
        // Required empty public constructor
    }

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        initViews(view);

        getInfo();

        return view;
    }

    private void initViews(View view){

        notifyGroupLeaving = view.findViewById(R.id.notifyGroupLeaving);
        loadingDialog = new ProgressDialog(getContext());
        loadingDialog.setTitle("Loading");
        loadingDialog.setMessage("Loading Data. Please Wait");
        userNameText = view.findViewById(R.id.userNameText);
        userNumberText = view.findViewById(R.id.userNumberText);
        userAboutText = view.findViewById(R.id.userAboutText);
        editNameImg = view.findViewById(R.id.editNameImg);
        editNumberImg = view.findViewById(R.id.editNumberImg);
        editAboutImg = view.findViewById(R.id.editAboutImg);
        userImageImg = view.findViewById(R.id.userImageImg);
        editImageImg = view.findViewById(R.id.editImageImg);
        autoReplySwitch = view.findViewById(R.id.autoReplySwitch);
        busyModeSwitch = view.findViewById(R.id.busyModeSwitch);
        screenshotPreventionSwitch = view.findViewById(R.id.screenshotPreventionSwitch);
        lastSeenSwitch = view.findViewById(R.id.lastSeenSwitch);

        editNameImg.setVisibility(View.VISIBLE);
        editImageImg.setVisibility(View.VISIBLE);
        editAboutImg.setVisibility(View.VISIBLE);
        editNumberImg.setVisibility(View.GONE);

        userImageImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ImageViewingActivity.class).putExtra("URL", olderPicture));
            }
        });

        editImageImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(getContext(), UserProfileFragment.this);
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

        notifyGroupLeaving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference mDatabaseForGettingInfo = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                if (notifyGroupLeaving.isChecked()) {
                    mDatabaseForGettingInfo.child("notifyGroupLeaving").setValue(true);
                } else {
                    mDatabaseForGettingInfo.child("notifyGroupLeaving").setValue(false);
                }
            }
        });

        busyModeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference mDatabaseForBusyMode = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                if (busyModeSwitch.isChecked()) {
                    //mDatabaseForBusyMode.child("busyMode")
                    mDatabaseForBusyMode.child("busyMode").child("status").setValue(true);
                } else {
                    mDatabaseForBusyMode.child("busyMode").child("status").setValue(false);
                }
            }
        });

        lastSeenSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference mDatabaseForBusyMode = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                if (lastSeenSwitch.isChecked()) {
                    mDatabaseForBusyMode.child("lastSeenShow").child("status").setValue(true);
                } else {
                    mDatabaseForBusyMode.child("lastSeenShow").child("status").removeValue();
                }
            }
        });

        screenshotPreventionSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference mDatabaseForScreenshotPrevention = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("screenshotPreventionEnabled");

                if (screenshotPreventionSwitch.isChecked()) {
                    mDatabaseForScreenshotPrevention.setValue(true);
                } else {
                    mDatabaseForScreenshotPrevention.setValue(false);
                }
            }
        });

        view.findViewById(R.id.privacyLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String privacyPolicyUrl = "https://www.privacypolicyonline.com/live.php?token=RrU7G96TSt84ZWGZVQ9tCnuHDVoSDaTf";

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));
                startActivity(browserIntent);
            }
        });

        view.findViewById(R.id.contactUsLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    PackageManager manager = getActivity().getPackageManager();
                    PackageInfo info = manager.getPackageInfo(getActivity().getPackageName(), PackageManager.GET_ACTIVITIES);

                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    //intent.setType("text/html");
                    intent.setData(Uri.parse("mailto:"));
                    //intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"folksinc20@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                    intent.putExtra(Intent.EXTRA_TEXT, "Enter What you want to say: \n");
                    startActivity(Intent.createChooser(intent, "Send Email"));

                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        autoReplySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference mDatabaseForAutoReply = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                if (autoReplySwitch.isChecked()) {
                    //Means that autoReply is checked true

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                    //setting up the layout for alert dialog
                    View view1 = LayoutInflater.from(getContext()).inflate(R.layout.dialog_auto_reply_layout, null, false);

                    builder1.setView(view1);

                    AlertDialog ratingDialog = builder1.create();
                    ratingDialog.show();

                    EditText autoReplyMessageEdit = view1.findViewById(R.id.autoReplyMessageEdit);
                    autoReplyMessageEdit.setText(autoReplyMessage);
                    Button submitBtn = view1.findViewById(R.id.submitBtn);

                    submitBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (autoReplyMessageEdit.getText().toString().isEmpty()) {
                                autoReplyMessageEdit.setError("Enter a Message First");
                                return;
                            }

                            mDatabaseForAutoReply.child("autoReply").child("status").setValue(true);
                            autoReplySwitch.setChecked(true);
                            mDatabaseForAutoReply.child("autoReply").child("message").setValue(HelperClass.encrypt(String.valueOf(autoReplyMessageEdit.getText().toString())));

                            ratingDialog.dismiss();
                            
                        }
                    });

                    ratingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            //Toast.makeText(getContext(), "Canceled", Toast.LENGTH_SHORT).show();
                            autoReplySwitch.setChecked(false);
                        }
                    });
                } else {
                    mDatabaseForAutoReply.child("autoReply").child("status").setValue(false);
                    autoReplySwitch.setChecked(false);
                }
            }
        });

        checkLastSeenSwitch();
    }

    private void changeInfo(String heading, final String change, String previousData) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        //setting up the layout for alert dialog
        View view1 = LayoutInflater.from(getContext()).inflate(R.layout.change_user_info_alert_dialog, null, false);

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
                    Toast.makeText(getContext(), "Please fill the field above first", Toast.LENGTH_SHORT).show();
                } else {
                    if (change.equals("username")){

                        DatabaseReference mDatabaseForChaning = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        mDatabaseForChaning.child("userName").setValue(HelperClass.encrypt(editingEditText.getText().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                MDToast.makeText(getContext(), "Name Changed Successfully", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
                                dialog.dismiss();
                            }
                        });

                    } else if (change.equals("about")){
                        DatabaseReference mDatabaseForChaning = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        mDatabaseForChaning.child("about").setValue(HelperClass.encrypt(editingEditText.getText().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                MDToast.makeText(getContext(), "Status Changed Successfully", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
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

    private void checkLastSeenSwitch() {
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("lastSeenShow")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("status")) {
                                if (dataSnapshot.child("status").getValue(Boolean.class)) {
                                    lastSeenSwitch.setChecked(true);
                                } else {
                                    lastSeenSwitch.setChecked(false);
                                }
                            } else {
                                lastSeenSwitch.setChecked(false);
                            }
                        } else {
                            lastSeenSwitch.setChecked(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getInfo(){
        DatabaseReference mDatabaseForGettingInfo = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseForGettingInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("userName")){
                    String username = dataSnapshot.child("userName").getValue(String.class);

                    //Toast.makeText(getContext(), "Name: " + username, Toast.LENGTH_SHORT).show();
                    userNameText.setText(HelperClass.decrypt(username));
                }



                if (dataSnapshot.hasChild("busyMode")) {
                    if (dataSnapshot.child("busyMode").hasChild("status")) {
                        if (dataSnapshot.child("busyMode").child("status").getValue(Boolean.class)) {
                            busyModeSwitch.setChecked(true);
                            //view.findViewById(R.id.autoReplyLayout).setVisibility(View.VISIBLE);
                        } else {
                            busyModeSwitch.setChecked(false);
                            //autoReplySwitch.setChecked(false);
                           // view.findViewById(R.id.autoReplyLayout).setVisibility(View.GONE);
                        }
                    } else {
                        busyModeSwitch.setChecked(false);
                        //autoReplySwitch.setChecked(false);
                        //view.findViewById(R.id.autoReplyLayout).setVisibility(View.GONE);
                    }
                } else {
                    //autoReplySwitch.setChecked(false);
                    busyModeSwitch.setChecked(false);
                    //view.findViewById(R.id.autoReplyLayout).setVisibility(View.GONE);
                }

                if (dataSnapshot.hasChild("autoReply")) {

                    if (dataSnapshot.child("autoReply").hasChild("message")) {
                        autoReplyMessage = dataSnapshot.child("autoReply").child("message").getValue(String.class);
                        autoReplyMessage = HelperClass.decrypt(autoReplyMessage);
                    }

                    if (dataSnapshot.child("autoReply").hasChild("status")) {
                        if (dataSnapshot.child("autoReply").child("status").getValue(Boolean.class)) {

                            autoReplySwitch.setChecked(true);
                            //view.findViewById(R.id.autoReplyLayout).setVisibility(View.VISIBLE);
                        } else {
                            autoReplySwitch.setChecked(false);
                        }
//                        if (dataSnapshot.child("autoReply").hasChild("message")) {
//                            autoReplySwitch.setChecked(true);
//                        } else {
//                            mDatabaseForGettingInfo.child("autoReply").child("status").setValue(false);
//                            autoReplySwitch.setChecked(false);
//                        }

                    } else {
                        autoReplySwitch.setChecked(false);
                    }
                } else {
                    autoReplySwitch.setChecked(false);
                }

                if (dataSnapshot.hasChild("about")){
                    String about = dataSnapshot.child("about").getValue(String.class);
                    userAboutText.setText(HelperClass.decrypt(about));
                }

                if (dataSnapshot.hasChild("mobileNumber")){
                    String number = dataSnapshot.child("mobileNumber").getValue(String.class);
                    userNumberText.setText(HelperClass.decrypt(number));
                }

                if (dataSnapshot.hasChild("notifyGroupLeaving")) {

                    notifyGroupLeaving.setChecked(dataSnapshot.child("notifyGroupLeaving").getValue(Boolean.class));

                } else {
                    notifyGroupLeaving.setChecked(true);
                }

                if (dataSnapshot.hasChild("profileImage")){
                    String profileImage = dataSnapshot.child("profileImage").getValue(String.class);

                    profileImage = HelperClass.decrypt(profileImage);

                    olderPicture = profileImage;

                    try {
                        Glide.with(getContext()).load(profileImage).into(userImageImg);
                    } catch (Exception e){
                        e.printStackTrace();
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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

                imageLink = HelperClass.encrypt(imageLink);

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                mDatabase.child("profileImage").setValue(imageLink).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Image Successfully Uploaded", Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

            });

        }).addOnProgressListener(taskSnapshot -> {

        }).addOnFailureListener(e ->{

            loadingDialog.dismiss();
            Toast.makeText(getContext(), "Sorry Image was not uploaded: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
