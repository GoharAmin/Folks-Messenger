package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import codegradients.com.chatapp.Models.ContactModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.NewGroupMakingAdapter;

public class CreateNewGroupActivity extends AppCompatActivity {

    ImageView backBtn;
    EditText groupName;

    public static List<ContactModel> toAddMembers = new ArrayList<>();

    public static List<ContactModel> NewMembersForGroup = new ArrayList<>();

    RecyclerView userRecycler;
    NewGroupMakingAdapter usersAdapter;

    Button createGroup;

    ImageView addGroupImage;
    CircleImageView groupImage;

    Uri resultUri;
    Bitmap bitmap;
    int groupImageSelected = 0;

    ProgressDialog imageUploadingDialog;

    //Firebase Instances
    DatabaseReference mDatabaseForGroupInfo;
    FirebaseAuth mAuth;

    int countForAddingMembers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        addGroupImage = findViewById(R.id.add_group_image_new_group);
        groupImage = findViewById(R.id.group_image_new_group);
        createGroup = findViewById(R.id.create_grp_btn);

        imageUploadingDialog = new ProgressDialog(this);

        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(CreateNewGroupActivity.this);
            }
        });

        addGroupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(CreateNewGroupActivity.this);
            }
        });
        groupName = findViewById(R.id.group_name_edit_create_grp);

        toAddMembers = new ArrayList<>(MainActivity.Live_Contacts);
        userRecycler = findViewById(R.id.user_recycler_create_grp);
        usersAdapter = new NewGroupMakingAdapter(MainActivity.Live_Contacts, this);
        userRecycler.setAdapter(usersAdapter);
        userRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NewMembersForGroup.size() == 0) {
                    MDToast.makeText(CreateNewGroupActivity.this, "Please Select Some Members first.", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();
                    return;
                }

                if (groupName.getText().toString().isEmpty()) {
                    groupName.setError("Please Enter Group Name First");
                    return;
                }

                if (groupImageSelected == 0){
                    MDToast.makeText(CreateNewGroupActivity.this, "Please Select Group Image First.", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();
                    return;
                }

                try {

                    imageUploadingDialog.setTitle("Creating New Group");
                    imageUploadingDialog.setMessage("Please wait while we make your group");
                    imageUploadingDialog.show();

                    mDatabaseForGroupInfo = FirebaseDatabase.getInstance().getReference().child("Groups");

                    String groupKey = mDatabaseForGroupInfo.push().getKey();

                    final StorageReference Submit_Datareference = FirebaseStorage.getInstance().getReference("GroupImages").child(groupKey);

                    Submit_Datareference.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Submit_Datareference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String Image_Download_Link = uri.toString();

                                    HashMap Data = new HashMap();
                                    Data.put("groupId", groupKey);
                                    Data.put("groupName", groupName.getText().toString());
                                    Data.put("groupImage", Image_Download_Link);
                                    Data.put("createdOn", String.valueOf(System.currentTimeMillis()));

                                    mDatabaseForGroupInfo.child(groupKey).updateChildren(Data).addOnSuccessListener(new OnSuccessListener() {
                                        @Override
                                        public void onSuccess(Object o) {

                                            countForAddingMembers = 0;

                                            for (ContactModel mod: NewMembersForGroup){

                                                countForAddingMembers ++;

                                                mDatabaseForGroupInfo.child(groupKey).child("users").child(mod.getUserId()).setValue("user").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        if (countForAddingMembers == NewMembersForGroup.size()){

                                                            mDatabaseForGroupInfo.child(groupKey).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue("admin").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    imageUploadingDialog.dismiss();
                                                                    Toast.makeText(CreateNewGroupActivity.this, "Group Created", Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });

                                }
                            });
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            imageUploadingDialog.setProgress((int) progress);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateNewGroupActivity.this, "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();
                            imageUploadingDialog.dismiss();
                        }
                    });

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    resultUri = result.getUri();

                    groupImageSelected = 1;
                    Bitmap bm = null;
                    bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    groupImage.setImageBitmap(bitmap);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
