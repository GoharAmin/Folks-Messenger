package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.valdesekamdem.library.mdtoast.MDToast;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import codegradients.com.chatapp.Models.GroupMemberModelForToken;
import codegradients.com.chatapp.Models.GroupMessageModel;
import codegradients.com.chatapp.Models.MemberModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.MembersAdapterForGroupDetails;
import codegradients.com.chatapp.helper_classes.HelperClass;

public class GroupDetailActivity extends AppCompatActivity {

    boolean notifyGroupLeaving = true;

    String myName = "";

    String groupId = "", groupName = "";

    long memberCount = 0;
    long count = 0;

    Switch notificationSwitch;

    ImageView groupImage;
    ImageView changeGroupImage;
    TextView groupNameText;
    ImageView backBtn, editGroupName, addMembers;

    List<MemberModel> list = new ArrayList<>();
    ArrayList<String> membersIds = new ArrayList<>();
    RecyclerView membersRecycler;
    MembersAdapterForGroupDetails membersAdapterForGroupDetails;

    Uri resultUri;
    ProgressDialog imageUpdatingDialog;

    Boolean isAdmin = false;

    AVLoadingIndicatorView groupDetailavi;

    //Firebase Instances
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseForMembers;

    //setting and getting main activity
    private static GroupDetailActivity groupDetailActivity;

    public static GroupDetailActivity getGroupDetailActivity() {
        return groupDetailActivity;
    }

    private static void setGroupDetailActivity(GroupDetailActivity groupDetailActivity) {
        GroupDetailActivity.groupDetailActivity = groupDetailActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        mAuth = FirebaseAuth.getInstance();
        GroupDetailActivity.setGroupDetailActivity(this);

        groupId = getIntent().getStringExtra("Id");
        groupName = getIntent().getStringExtra("Name");

        notificationSwitch = findViewById(R.id.notification_switch_group_details);
        //getIfNotificationDisabled();
        getMyInfo();

        findViewById(R.id.leave_btn_group_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(GroupDetailActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to leave this group ? \nAll your data in the group will be lost.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (notifyGroupLeaving) {

                                    final String messag = HelperClass.encrypt("groupLeaving");

                                    String key = String.valueOf(System.currentTimeMillis());
                                    GroupMessageModel model = new GroupMessageModel(messag, groupId, mAuth.getCurrentUser().getUid(), key, HelperClass.encrypt(myName), String.valueOf(System.currentTimeMillis()), "groupLeaving");
                                    FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("Messages").child(key).setValue(model);

                                    for (GroupMemberModelForToken gpmMFT: GroupChatActivity.getGroupChatActivity().groupMemberModelForTokenList) {
                                        if (!gpmMFT.getPersonId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                            HelperClass.sendNotificationOfGroupLeaving(GroupDetailActivity.this, groupId, groupName, gpmMFT.getPersonName(), gpmMFT.getPersonToken());
                                        }
                                    }

//                                    for (String memberIdCurrent: membersIds) {
//
//                                        try {
//                                            DatabaseReference Notification_Reference = FirebaseDatabase.getInstance().getReference("GroupNotifications").child(groupId).child(groupName).child(memberIdCurrent).child(myName).child(HelperClass.decrypt(messag));
//                                            String Notif_Id = Notification_Reference.push().getKey();
//                                            HashMap Notif_map = new HashMap();
//                                            Notif_map.put("To", memberIdCurrent);
//                                            Notif_map.put("From", FirebaseAuth.getInstance().getCurrentUser().getUid());
//                                            Notif_map.put("Type", "GroupMessage");
//
//                                            Log.v("GroupNotngSent__", "Data: " + Notif_map);
//
//                                            Notification_Reference.child(Notif_Id).updateChildren(Notif_map);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
                                }

                                DatabaseReference mDatabaseForRemoving = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("users");
                                mDatabaseForRemoving.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
//                                        DatabaseReference mDatabaseFor = FirebaseDatabase.getInstance().getReference().child("GroupUsers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(groupId);
//                                        mDatabaseFor.removeValue();

                                        GroupChatActivity.getGroupChatActivity().finish();

                                        finish();
                                        //GroupDetailActivity.getGroupDetailActivity().getMembers();
                                    }
                                });
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
        imageUpdatingDialog = new ProgressDialog(this);

        groupDetailavi = findViewById(R.id.avi_group_detail);
        groupDetailavi.setVisibility(View.VISIBLE);


        addMembers = findViewById(R.id.add_members_group_detail);
        addMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupDetailActivity.this, AddNewMembersToGroupActivity.class).putStringArrayListExtra("Members", membersIds).putExtra("GroupId", groupId).putExtra("GroupName", groupName));
            }
        });

        groupImage = findViewById(R.id.group_image_group_detail);
        changeGroupImage = findViewById(R.id.change_group_image_group_detail);
        changeGroupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(GroupDetailActivity.this);
            }
        });
        getGroupImageAndName();

        groupNameText = findViewById(R.id.grp_name_text_detail);
        groupNameText.setText(groupName);

        backBtn = findViewById(R.id.back_btn_grp_detail);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editGroupName = findViewById(R.id.edit_group_name_btn);
        editGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeNameDialog();
            }
        });

        membersRecycler = findViewById(R.id.members_recycler);
        membersAdapterForGroupDetails = new MembersAdapterForGroupDetails(list, this, groupId, isAdmin);
        membersRecycler.setAdapter(membersAdapterForGroupDetails);
        membersRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getMyInfo() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("notifyGroupLeaving")) {
                    notifyGroupLeaving = dataSnapshot.child("notifyGroupLeaving").getValue(Boolean.class);
                } else {
                    notifyGroupLeaving = true;
                }

                if (dataSnapshot.hasChild("userName")) {
                    myName = HelperClass.decrypt(dataSnapshot.child("userName").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getGroupImageAndName() {
        try {
            DatabaseReference mDatabaseForImageAndName = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);
            mDatabaseForImageAndName.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String groupNamee = dataSnapshot.child("groupName").getValue().toString();
                    groupNameText.setText(groupNamee);

                    if (dataSnapshot.hasChild("groupImage")) {

                        if (!isFinishing()) {
                            final String groupImagee = dataSnapshot.child("groupImage").getValue().toString();
                            Glide.with(GroupDetailActivity.this).load(groupImagee).into(groupImage);

                            groupImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //startActivity(new Intent(GroupDetailActivity.this, UserImageViewingActivity.class).putExtra("url", groupImagee));
                                }
                            });
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMembers() {

        list.clear();
        try {
            mDatabaseForMembers = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("users");
            mDatabaseForMembers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    list.clear();
                    membersIds.clear();

                    memberCount = dataSnapshot.getChildrenCount();

                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        count++;
                        final MemberModel model = new MemberModel();

                        String memberId = d.getKey();
                        String memberStatus = d.getValue().toString();

                        if (memberId.equals(mAuth.getCurrentUser().getUid())) {

                            if (memberStatus.equals("admin")) {
                                changeGroupImage.setVisibility(View.VISIBLE);
                                editGroupName.setVisibility(View.VISIBLE);
                                addMembers.setVisibility(View.VISIBLE);
                                isAdmin = true;
                            } else {
                                isAdmin = false;
                                changeGroupImage.setVisibility(View.GONE);
                                editGroupName.setVisibility(View.GONE);
                                addMembers.setVisibility(View.GONE);
                            }

                        }

                        model.setId(memberId);
                        model.setStatus(memberStatus);

                        membersIds.add(memberId);

                        DatabaseReference mDatabaseForMember = FirebaseDatabase.getInstance().getReference().child("users").child(memberId);
                        mDatabaseForMember.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot ddataSnapshot) {
                                String name = HelperClass.decrypt(ddataSnapshot.child("userName").getValue().toString());
                                String image = "";

                                if (ddataSnapshot.hasChild("profileImage")) {
                                    image = HelperClass.decrypt(ddataSnapshot.child("profileImage").getValue().toString());
                                }

                                model.setName(name);
                                model.setImage(image);

                                list.add(model);

                                membersAdapterForGroupDetails.notifyDataSetChanged();
                                membersAdapterForGroupDetails = new MembersAdapterForGroupDetails(list, GroupDetailActivity.this, groupId, isAdmin);
                                membersRecycler.setAdapter(membersAdapterForGroupDetails);

                                if (count == memberCount) {
                                    groupDetailavi.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showChangeNameDialog() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(GroupDetailActivity.this);
        //setting up the layout for alert dialog
        View view1 = LayoutInflater.from(GroupDetailActivity.this).inflate(R.layout.change_group_name_alert_dialog, null, false);

        builder1.setView(view1);

        final TextView headingTextView = view1.findViewById(R.id.heading_edit_dialog);

        final EditText editingEditText = view1.findViewById(R.id.change_grp_name_edit);
        editingEditText.setText(groupName);
        editingEditText.setSelection(editingEditText.getText().length());
        // Set up the buttons
        builder1.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {

                if (editingEditText.getText().toString().isEmpty()) {
                    Toast.makeText(GroupDetailActivity.this, "Enter new name first", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        DatabaseReference mDatabaseForGroupNameUpdating = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("groupName");
                        mDatabaseForGroupNameUpdating.setValue(editingEditText.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                MDToast.makeText(GroupDetailActivity.this, "Group Name Changed Successfully.", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();

                                dialog.dismiss();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
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

    @Override
    protected void onResume() {
        super.onResume();

        list.clear();
        getMembers();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();

                uploadGroupImage();

            }
        }
    }

    private void uploadGroupImage() {

        try {
            imageUpdatingDialog.setTitle("Uploading new Group Photo");
            imageUpdatingDialog.setMessage("Please wait while we change your group photo");
            imageUpdatingDialog.show();

            final StorageReference Submit_Datareference = FirebaseStorage.getInstance().getReference("GroupImages").child(groupId);

            Submit_Datareference.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Submit_Datareference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String Image_Download_Link = uri.toString();

                            DatabaseReference mDatabaseReferenceForImageUpdating = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("groupImage");
                            mDatabaseReferenceForImageUpdating.setValue(Image_Download_Link);

                            imageUpdatingDialog.dismiss();

                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    imageUpdatingDialog.setProgress((int) progress);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GroupDetailActivity.this, "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();
                    imageUpdatingDialog.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
