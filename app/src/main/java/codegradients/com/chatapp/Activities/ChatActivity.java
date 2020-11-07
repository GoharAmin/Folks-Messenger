package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.sinch.android.rtc.calling.Call;
import com.theartofdev.edmodo.cropper.CropImage;
import com.valdesekamdem.library.mdtoast.MDToast;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import codegradients.com.chatapp.Activities.SinchScreens.BaseActivity;
import codegradients.com.chatapp.Activities.SinchScreens.CallScreenActivity;
import codegradients.com.chatapp.Models.ContactModel;
import codegradients.com.chatapp.Models.ForwardModel;
import codegradients.com.chatapp.Models.ParticipantModel;
import codegradients.com.chatapp.helper_classes.HelperClass;
import codegradients.com.chatapp.helper_classes.SinchService;
import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerConst;
import codegradients.com.chatapp.Models.ChatModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.ChatAdapter;

public class ChatActivity extends BaseActivity {

    public static List<ForwardModel> listForSelectedModels = new ArrayList<>();

    boolean sendNow = true;

    boolean otherBlockedYou = false, youBlocked = false;

    String otherPersonToken = "";

    public static String receiverId = "", receiverName = "", receiverImage = "";
    TextView userNameText, userOnlineText;
    CircleImageView userImageView;
    EditText messageEdit;
    FloatingActionButton sendBtn;

    ImageView attachBtn;

    AVLoadingIndicatorView aviChat;
    RecyclerView messagesRecycler;
    ChatAdapter chatAdapter;

    DatabaseReference Database_Reference, mDatabaseReferenceOfFriendToAddMessage;

    public static String myName = "";
    public static String myImage = "";

    private boolean isMediaBlockedByOtherPerson = false;

    private boolean otherBusyModeOn = false;
    private boolean otherAutoReplyModeOn = false;
    private String otherAutoReplyMessage = "";

    boolean isFirstMessage = true;

    boolean autoDestructionStatus = false;
    String autoDestructionTime = "0";

    boolean receiverAutoDestructionStatus = false;
    String receiverAutoDestructionTime = "0";

    boolean isScreenshotPreventionEnabled = false;

    List<String> memojisList = new ArrayList<>();

    BottomSheetDialog emojisDialog;

    public static boolean isSelectionModeOn = false;

    @Override
    protected void onPause() {
        super.onPause();

        //ScreenshotPreventionMyApplcation.getInstance().unregisterScreenshotObserver();
    }

    RelativeLayout mainLayout, friendRequestLayout, notFriendLayout;

    //setting and getting main activity
    private static ChatActivity chatActivity;

    public static ChatActivity getChatActivity() {
        return chatActivity;
    }

    private static void setChatActivity(ChatActivity chatActivity) {
        ChatActivity.chatActivity = chatActivity;
    }

    public static RelativeLayout selectionOptionsLayoutCard;
    public static ImageView forwardIconCard, deleteIconCard, copyIconCard, replyIcon;

    LinearLayout headerLayoutOnTop;

    public String replyMessageSenderName = "", replyMessageText = "", replyMessageImageLink = "", replyMessageSelectedTime = "";
    RelativeLayout replyLayoutChat;
    TextView replySenderNameViewChat, replyMessageTextViewChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ChatActivity.setChatActivity(this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        sendNow = true;

        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("userName");
        receiverImage = getIntent().getStringExtra("userImage");

        HelperClass.chattingWithId = receiverId;

        getOtherPersonInfo();
        getMediaBlockByFriend();
        getScreenshotPreventionStatus();
        getLastSeenStatus();

        initViews();

        getChatMessages();

        showAttachDialog();
        showEmojiDialog();

        getBlockStatus();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            getMyInfo();
            findViewById(R.id.callIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(ChatActivity.this, "Please Connect to an active Connection in order to make a call", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (otherBusyModeOn) {
                        MDToast.makeText(ChatActivity.this, "Your Friend Is Busy At This Moment", MDToast.LENGTH_LONG, MDToast.TYPE_INFO).show();
                        return;
                    }

                    getSinchServiceInterface().startClient();

                    //Call call = getSinchServiceInterface().callUserAudio(receiverId);
                    Call call = getSinchServiceInterface().callUserAudio(receiverId);
                    String callId = call.getCallId();

                    Intent callScreen = new Intent(ChatActivity.this, CallScreenActivity.class);
                    callScreen.putExtra(SinchService.CALL_ID, callId);
                    callScreen.putExtra("callerName", receiverName);
                    startActivity(callScreen);

                    HashMap hashMapData = new HashMap();

                    hashMapData.put("callerId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    hashMapData.put("calleeId", receiverId);
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

            findViewById(R.id.videoIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isNetworkAvailable()) {
                        Toast.makeText(ChatActivity.this, "Please Connect to an active Connection in order to make a call", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (otherBusyModeOn) {
                        MDToast.makeText(ChatActivity.this, "Your Friend Is Busy At This Moment", MDToast.LENGTH_LONG, MDToast.TYPE_INFO).show();
                        return;
                    }

                    getSinchServiceInterface().startClient();

                    Call call = getSinchServiceInterface().callUserVideo(receiverId);
                    String callId = call.getCallId();

                    Intent callScreen = new Intent(ChatActivity.this, CallScreenActivity.class);
                    callScreen.putExtra(SinchService.CALL_ID, callId);
                    callScreen.putExtra("callerName", receiverName);
                    startActivity(callScreen);

                    HashMap hashMapData = new HashMap();

                    hashMapData.put("callerId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    hashMapData.put("calleeId", receiverId);
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
        }

        findViewById(R.id.emojiIconChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emojisDialog.show();
            }
        });

        getMemojis();
        checkIfHeIsAlreadyAFriendOrNot();
    }

    //getting other person data and my data
    private void getOtherPersonInfo(){
        DatabaseReference mDatabaseForGettingOtherPersonToken = FirebaseDatabase.getInstance().getReference().child("users").child(receiverId);
        mDatabaseForGettingOtherPersonToken.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("selfDestructMessage")) {
                    if (dataSnapshot.child("selfDestructMessage").hasChild(receiverId)) {
                        receiverAutoDestructionStatus = dataSnapshot.child("selfDestructMessage").child(receiverId).child("status").getValue(Boolean.class);
                        receiverAutoDestructionTime = dataSnapshot.child("selfDestructMessage").child(receiverId).child("time").getValue(String.class);
                    }
                }

//                if (dataSnapshot.hasChild("screenshotPreventionEnabled")) {
//                    isScreenshotPreventionEnabled = dataSnapshot.child("screenshotPreventionEnabled").getValue(Boolean.class);
//
//                    if (isScreenshotPreventionEnabled) {
//                        screenshotDetectionOn();
//                    }
//                }
//
//                if (isScreenshotPreventionEnabled) {
//                    ScreenshotPreventionMyApplcation.getInstance().registerScreenshotObserver();
//                    ScreenshotPreventionMyApplcation.getInstance().allowUserSaveScreenshot(false);
//                } else {
//                    ScreenshotPreventionMyApplcation.getInstance().unregisterScreenshotObserver();
//                }

                if (dataSnapshot.hasChild("busyMode")) {
                    if (dataSnapshot.child("busyMode").hasChild("status")) {
                        otherBusyModeOn = dataSnapshot.child("busyMode").child("status").getValue(Boolean.class);
                    }
                }

                if (dataSnapshot.hasChild("autoReply")) {
                    if (dataSnapshot.child("autoReply").hasChild("status")) {
                        otherAutoReplyModeOn = dataSnapshot.child("autoReply").child("status").getValue(Boolean.class);
                    }

                    if (dataSnapshot.child("autoReply").hasChild("message")) {
                        otherAutoReplyMessage = dataSnapshot.child("autoReply").child("message").getValue(String.class);
                        otherAutoReplyMessage = HelperClass.decrypt(otherAutoReplyMessage);
                    }
                }

                if (dataSnapshot.hasChild("Device_Token")) {
                    otherPersonToken = dataSnapshot.child("Device_Token").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getMediaBlockByFriend() {
        FirebaseDatabase.getInstance().getReference().child("users").child(receiverId).child("mediaBlockedContacts")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.v("mediaBlockCheck__", "DataSnapShot Exists");
                            if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                Log.v("mediaBlockCheck__", "Media Blocked");
                                isMediaBlockedByOtherPerson = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(Boolean.class);
                            } else {
                                Log.v("mediaBlockCheck__", "Media is not Blocked");
                                isMediaBlockedByOtherPerson = false;
                            }
                        } else {
                            Log.v("mediaBlockCheck__", "DataSnapShot Does Not Exists");
                            isMediaBlockedByOtherPerson = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getLastSeenStatus() {
        FirebaseDatabase.getInstance().getReference().child("users").child(receiverId).child("lastSeenShow")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        lastSeenManageScheduler = Executors.newSingleThreadScheduledExecutor();
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("status")) {
                                if (dataSnapshot.child("status").getValue(Boolean.class)) {
                                    userOnlineText.setVisibility(View.GONE);
                                    manageLastSeen(false);
                                } else {
                                    userOnlineText.setVisibility(View.VISIBLE);
                                    manageLastSeen(true);
                                }
                            } else {
                                userOnlineText.setVisibility(View.VISIBLE);
                                manageLastSeen(true);
                            }
                        } else {
                            userOnlineText.setVisibility(View.VISIBLE);
                            manageLastSeen(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getScreenshotPreventionStatus() {
        FirebaseDatabase.getInstance().getReference().child("users").child(receiverId).child("screenshotPreventionEnabled")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            isScreenshotPreventionEnabled = dataSnapshot.getValue(Boolean.class);
                        } else {
                            isScreenshotPreventionEnabled = false;
                        }

                        if (isScreenshotPreventionEnabled) {
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                        } else {
                            //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                        }

//                        if (isScreenshotPreventionEnabled) {
//                            screenshotDetectionOn();
//                            ScreenshotPreventionMyApplcation.getInstance().registerScreenshotObserver();
//                            ScreenshotPreventionMyApplcation.getInstance().allowUserSaveScreenshot(false);
//                        } else {
//                            ScreenshotPreventionMyApplcation.getInstance().unregisterScreenshotObserver();
//                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getMemojis() {
        FirebaseDatabase.getInstance().getReference().child("Memojis").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot d: dataSnapshot.getChildren()) {
                            try {
                                String url = d.getValue(String.class);
                                memojisList.add(url);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void showEmojiDialog() {
        emojisDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.emoji_dialog_layout, null);

        class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {

            @NonNull
            @Override
            public EmojiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(ChatActivity.this).inflate(R.layout.item_emoji_dialog, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull EmojiAdapter.ViewHolder holder, int position) {
                Glide.with(ChatActivity.this).
                        load(memojisList.get(position)).
                        apply(new RequestOptions().placeholder(R.drawable.progress_animation).error(R.drawable.ic_baseline_error_outline_24)).
                        into(holder.emojiIconImage);

                holder.emojiIconImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(ChatActivity.this, "" + memojisList.get(position), Toast.LENGTH_SHORT).show();
                        sendEmojiMethod(memojisList.get(position));
                        //emojisDialog.dismiss();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return memojisList.size();
            }

            class ViewHolder extends RecyclerView.ViewHolder {

                ImageView emojiIconImage;
                public ViewHolder(@NonNull View itemView) {
                    super(itemView);
                    emojiIconImage = itemView.findViewById(R.id.emojiIconEmojiRecycler);
                }
            }
        }

        RecyclerView emojisRecycler = sheetView.findViewById(R.id.emojisRecycler);
        emojisRecycler.setAdapter(new EmojiAdapter());
        emojisRecycler.setLayoutManager(new GridLayoutManager(ChatActivity.this, 4));

        emojisDialog.setContentView(sheetView);
    }

    private void sendEmojiMethod(String Image_Download_Link) {
        final String key = String.valueOf(System.currentTimeMillis());

        ChatModel model = new ChatModel(HelperClass.encrypt(Image_Download_Link), HelperClass.encrypt(myName), FirebaseAuth.getInstance().getCurrentUser().getUid(), HelperClass.encrypt(receiverName), receiverId, String.valueOf(key), key, "img", false, false, "sent", "0", "0", autoDestructionStatus, autoDestructionTime);

        if (!replyMessageText.equals("")) {
            model.setReplySenderMessageName(replyMessageSenderName);
            model.setReplyMessageImageLink(replyMessageImageLink);
            model.setReplyMessageText(replyMessageText);
            model.setReplyMessageSelectedTime(replyMessageSelectedTime);
        }
        replyLayoutChat.setVisibility(View.GONE);

        Database_Reference.child(key).setValue(model);//storing actual msg with name of the user
        mDatabaseReferenceOfFriendToAddMessage.child(key).setValue(model);//storing actual msg with name of the user

        if (model.getAutoDestructionStatus()) {
            autoDestroyMessageTimer(model);
        }

        if (!otherBusyModeOn) {
            Send_Notification("Image");
        }

        if (otherAutoReplyModeOn) {
            sendAutoReply();
        }

//        if (messageSendingTime == 0){
//        } else {
//            sendScheduledNotification("Image", model);
//        }
    }

    private void getMyInfo() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("userName")) {
                    myName = HelperClass.decrypt(dataSnapshot.child("userName").getValue(String.class));
                }

                if (dataSnapshot.hasChild("profileImage")) {
                    myImage = HelperClass.decrypt(dataSnapshot.child("profileImage").getValue(String.class));
                }

                if (dataSnapshot.hasChild("selfDestructMessage")) {
                    if (dataSnapshot.child("selfDestructMessage").hasChild(receiverId)) {
                        autoDestructionStatus = dataSnapshot.child("selfDestructMessage").child(receiverId).child("status").getValue(Boolean.class);
                        autoDestructionTime = dataSnapshot.child("selfDestructMessage").child(receiverId).child("time").getValue(String.class);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showConferenceCallStartingDialog() {
        try {

            AlertDialog.Builder builder1 = new AlertDialog.Builder(ChatActivity.this);
            //setting up the layout for alert dialog
            View view1 = LayoutInflater.from(ChatActivity.this).inflate(R.layout.dialog_add_more_participants_layout, null, false);

            builder1.setView(view1);

            AlertDialog ratingDialog = builder1.create();

            Button cancelBtn = view1.findViewById(R.id.cancelBtn);
            Button startBtn = view1.findViewById(R.id.startCallBtn);

            RecyclerView participantsRecycler = view1.findViewById(R.id.participantsRecycler);

            List<ParticipantModel> contactsModelListParticipants = new ArrayList<>();

            for (ContactModel contactModel: MainActivity.Live_Contacts) {
                contactsModelListParticipants.add(new ParticipantModel(contactModel.getUserId(), contactModel.getUserName(), contactModel.getUserNumber(), contactModel.getUserImage(), contactModel.getUserAbout(), contactModel.getUserToken(), false));
            }

            class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> {

                @NonNull
                @Override
                public ParticipantsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    return new ViewHolder(LayoutInflater.from(ChatActivity.this).inflate(R.layout.item_contact_layout_participant_conference, parent, false));
                }

                @Override
                public void onBindViewHolder(@NonNull ParticipantsAdapter.ViewHolder holder, int position) {
                    ParticipantModel mo = contactsModelListParticipants.get(position);
                    Glide.with(ChatActivity.this).load(mo.getUserImage()).into(holder.userImageParticipantCard);
                    holder.checkBoxParticipantCard.setText(mo.getUserName());

                    holder.checkBoxParticipantCard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) {
                                mo.setChecked(true);
                            } else {
                                mo.setChecked(false);
                            }
                        }
                    });

                    if (mo.getUserId().equals(receiverId)) {
                        mo.setChecked(true);
                        holder.checkBoxParticipantCard.setChecked(true);
                    }
                }

                @Override
                public int getItemCount() {
                    return contactsModelListParticipants.size();
                }

                class ViewHolder extends RecyclerView.ViewHolder {

                    CircleImageView userImageParticipantCard;
                    CheckBox checkBoxParticipantCard;

                    public ViewHolder(@NonNull View itemView) {
                        super(itemView);

                        userImageParticipantCard = itemView.findViewById(R.id.userImageParticipant);
                        checkBoxParticipantCard = itemView.findViewById(R.id.userNameParticipant);
                    }
                }
            }

            ParticipantsAdapter participantsAdapter = new ParticipantsAdapter();
            participantsRecycler.setAdapter(participantsAdapter);
            participantsRecycler.setLayoutManager(new LinearLayoutManager(ChatActivity.this));

            //ratingDialog.setCancelable(true);
            //ratingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            ratingDialog.show();

            startBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    for (ParticipantModel mm: contactsModelListParticipants) {
                        if (mm.isChecked()) {
                            Log.v("participants__", "Contact: " + mm.getUserName());
                        } else {
                            Log.v("participants__", "Contact Not: " + mm.getUserName());
                        }
                    }
                }
            });

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ratingDialog.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkIfHeIsAlreadyAFriendOrNot() {
        DatabaseReference mDatabaseForChecking = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseForChecking.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotOfFrind) {

                boolean isFriend = false;
                //Chekcing If he is already our friend

                if (dataSnapshotOfFrind.hasChild("friends")){

                    if (dataSnapshotOfFrind.child("friends").hasChild(receiverId)) {

                        isFriend = true;

                        notFriendLayout.setVisibility(View.GONE);
                        friendRequestLayout.setVisibility(View.GONE);
                        mainLayout.setVisibility(View.VISIBLE);
                    }
                }

                if (!isFriend){

                    //Checking if he has sent a request to us
                    DatabaseReference mDatabaseForRequestChecking = FirebaseDatabase.getInstance().getReference().child("friendRequest");
                    mDatabaseForRequestChecking.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            boolean heSentRequest = false;
                            boolean youSentRequest = false;

                            if (dataSnapshot.hasChild(receiverId)) {
                                if (dataSnapshot.child(receiverId).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    //Means He have sent us a request
                                    heSentRequest = true;

                                    notFriendLayout.setVisibility(View.GONE);
                                    friendRequestLayout.setVisibility(View.VISIBLE);
                                    mainLayout.setVisibility(View.GONE);

//                                    String requestsenderId = d.child("senderId").getValue(String.class);
//                                    String requestreceiverId = d.child("receiverId").getValue(String.class);
//                                    String requestsenderName = d.child("senderName").getValue(String.class);
//                                    String requestsenderImage = d.child("senderImage").getValue(String.class);
//                                    String requestreceiverImage = d.child("receiverImage").getValue(String.class);
//                                    String requestrequestSendingTime = d.child("time").getValue(String.class);
//                                    String requeststatus = d.child("status").getValue(String.class);

                                    CircleImageView requesterImage = findViewById(R.id.requesterImage);
                                    TextView requesterName = findViewById(R.id.requesterName);
                                    Button acceptBtn = findViewById(R.id.acceptBtn);
                                    Button declineBtn = findViewById(R.id.declineBtn);

                                    try {
                                        Glide.with(ChatActivity.this).load(receiverImage).into(requesterImage);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    requesterName.setText(receiverName);

                                    HashMap data = new HashMap();
                                    data.put("receiverId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    data.put("receiverName", getSharedPreferences("chatAppSP", MODE_PRIVATE).getString("myName", ""));
                                    data.put("receiverImage", getSharedPreferences("chatAppSP", MODE_PRIVATE).getString("myImage", ""));
                                    data.put("senderId", receiverId);
                                    data.put("senderImage", receiverImage);
                                    data.put("senderName", receiverName);
                                    data.put("time", String.valueOf(System.currentTimeMillis()));

                                    Log.v("FriendReq__", "Here Name: " + receiverName);

                                    acceptBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            DatabaseReference mDatabaseForAcceptingRequest = FirebaseDatabase.getInstance().getReference().child("users");
                                            mDatabaseForAcceptingRequest.child(receiverId).child("friends").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                                            mDatabaseForAcceptingRequest.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends").child(receiverId).setValue(true);
                                            mDatabaseForRequestChecking.child(receiverId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue("Accepted");

                                            data.put("status", "accepted");

                                            HelperClass.sendNotificationOfChatRequestStatus(ChatActivity.this, data, otherPersonToken, 0);

                                            Intent chatActivityIntent = new Intent(ChatActivity.this, ChatActivity.class);
                                            chatActivityIntent.putExtra("userId", receiverId);
                                            chatActivityIntent.putExtra("userName", receiverName);
                                            chatActivityIntent.putExtra("userImage", receiverImage);
                                            startActivity(chatActivityIntent);
                                            finish();
                                        }
                                    });

                                    declineBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            data.put("status", "declined");

                                            HelperClass.sendNotificationOfChatRequestStatus(ChatActivity.this, data, otherPersonToken, 1);

                                            mDatabaseForRequestChecking.child(receiverId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                                            Intent chatActivityIntent = new Intent(ChatActivity.this, ChatActivity.class);
                                            chatActivityIntent.putExtra("userId", receiverId);
                                            chatActivityIntent.putExtra("userName", receiverName);
                                            chatActivityIntent.putExtra("userImage", receiverImage);
                                            startActivity(chatActivityIntent);
                                            finish();
                                        }
                                    });
                                }
                            }

                            if (!heSentRequest) {
                                if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild(receiverId)) {

                                        youSentRequest = true;
                                        //Means You have sent that person a friend Request
                                        notFriendLayout.setVisibility(View.VISIBLE);
                                        friendRequestLayout.setVisibility(View.GONE);
                                        mainLayout.setVisibility(View.GONE);

                                        DataSnapshot d = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(receiverId);

                                        String requestsenderId = d.child("senderId").getValue(String.class);
                                        String requestreceiverId = d.child("receiverId").getValue(String.class);
                                        String requestsenderName = d.child("senderName").getValue(String.class);
                                        String requestsenderImage = d.child("senderImage").getValue(String.class);
                                        String requestreceiverImage = d.child("receiverImage").getValue(String.class);
                                        String requestSendingTime = d.child("time").getValue(String.class);
                                        String status = d.child("status").getValue(String.class);

                                        CircleImageView requestReceiverImage = findViewById(R.id.receiverImage);
                                        try {
                                            Glide.with(ChatActivity.this).load(receiverImage).into(requestReceiverImage);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        TextView requestReceiverName = findViewById(R.id.receiverName);
                                        requestReceiverName.setText(receiverName);
                                        TextView statusRequest = findViewById(R.id.statusRequest);
                                        Button sendRequest = findViewById(R.id.sendRequestBtn);

                                        if (status.equals("sent")) {
                                            statusRequest.setText("You have already Sent Him a request");
                                            sendRequest.setVisibility(View.GONE);
                                        } else {
                                            sendRequest.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }

                            }

                            if (!heSentRequest && !youSentRequest) {

                                notFriendLayout.setVisibility(View.VISIBLE);
                                friendRequestLayout.setVisibility(View.GONE);
                                mainLayout.setVisibility(View.GONE);

                                CircleImageView requestReceiverImage = findViewById(R.id.receiverImage);
                                try {
                                    Glide.with(ChatActivity.this).load(receiverImage).into(requestReceiverImage);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                TextView requestReceiverName = findViewById(R.id.receiverName);
                                requestReceiverName.setText(receiverName);
                                TextView statusRequest = findViewById(R.id.statusRequest);
                                Button sendRequest = findViewById(R.id.sendRequestBtn);
                                sendRequest.setVisibility(View.VISIBLE);

                                sendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        HashMap data = new HashMap();
                                        data.put("senderId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                        data.put("senderName", getSharedPreferences("chatAppSP", MODE_PRIVATE).getString("myName", ""));
                                        data.put("senderImage", getSharedPreferences("chatAppSP", MODE_PRIVATE).getString("myImage", ""));
                                        data.put("receiverId", receiverId);
                                        data.put("receiverImage", receiverImage);
                                        data.put("receiverName", receiverName);
                                        data.put("status", "sent");
                                        data.put("time", String.valueOf(System.currentTimeMillis()));

                                        statusRequest.setText("You have already Sent Him a request");
                                        sendRequest.setVisibility(View.GONE);

                                        mDatabaseForRequestChecking.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(receiverId).updateChildren(data);
                                        HelperClass.sendFriendRequestNotification(ChatActivity.this, data, otherPersonToken);
                                    }
                                });
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
    }

    BottomSheetDialog attachDialog;

    String mCameraFileName = "";

    private void showAttachDialog() {

        attachDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.sending_options_layout, null);

        LinearLayout imageLayout = sheetView.findViewById(R.id.image_type_selected);
        LinearLayout videoLayout = sheetView.findViewById(R.id.vid_type_selected);
        LinearLayout audioLayout = sheetView.findViewById(R.id.audio_type_selected);
        LinearLayout docLayout = sheetView.findViewById(R.id.docuemnt_type_selected);
        //LinearLayout linearLayout_test = sheetView.findViewById(R.id.linearLayout_recordAudoi);


        sheetView.findViewById(R.id.linearLayout_recordAudoi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, RecordAudioActivity.class);
                intent.putExtra(RecordAudioActivity.KEY_ACTIVITY_TYPE, "ch");
                startActivityForResult(intent, 1122);
                attachDialog.dismiss();
            }
        });

        imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                CropImage.activity()
//                        .setAspectRatio(1, 1)
//                        .start(ChatActivity.this);

                String[] colors = {"Camera", "Gallery"};
//
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                    builder.setTitle("Pick A Photo");
                    builder.setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // the user clicked on colors[which]
                            if (which == 0) {
//                                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                                startActivityForResult(takePicture, 1921);

                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

                                    Calendar c = Calendar.getInstance();
                                    System.out.println("Current time => "+c.getTime());

                                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
                                    String formattedDate = df.format(c.getTime());

                                    String newPicFile ="IMG-" + formattedDate + ".jpg";
                                    String outPath = "/sdcard/" + newPicFile;
                                    File outFile = new File(outPath);

                                    mCameraFileName = outFile.toString();
                                    Uri outuri = Uri.fromFile(outFile);
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                                    startActivityForResult(intent, 1922);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else if (which == 1){
                                try {
                                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(pickPhoto , 1921);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                    builder.show();

                attachDialog.dismiss();
            }
        });

        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                    return;
                }
                Intent intent = new Intent();
                intent.setType("video/mp4");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select videos"), 1321);
                attachDialog.dismiss();
            }
        });

        audioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                    return;
                }

                Intent intent;
                intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/mpeg");
                startActivityForResult(Intent.createChooser(intent, "Pick Audio File"), 1231);
                attachDialog.dismiss();
            }
        });

        docLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                    return;
                }

                Intent intent;
                intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/*");
                startActivityForResult(Intent.createChooser(intent, "Pick DOC File"), 1232);
                attachDialog.dismiss();
            }
        });

        attachDialog.setContentView(sheetView);
    }

    List<String> emojisList = new ArrayList<>();

    private void getBlockStatus() {
        DatabaseReference mDatabaseToGetStatus = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseToGetStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("BlockedUsers")) {
                    if (dataSnapshot.child("BlockedUsers").hasChild(receiverId)) {
                        Boolean Status = dataSnapshot.child("BlockedUsers").child(receiverId).getValue(Boolean.class);
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

                DatabaseReference mDatabaseToGetOtherBlockStatus = FirebaseDatabase.getInstance().getReference().child("users").child(receiverId);
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

                            if (!youBlocked && !otherBlockedYou) {
                                messageEdit.setEnabled(true);
                                attachBtn.setEnabled(true);
                                findViewById(R.id.callIcon).setEnabled(true);
                            } else {
                                messageEdit.setEnabled(false);
                                attachBtn.setEnabled(false);

                                findViewById(R.id.callIcon).setEnabled(false);

                                if (youBlocked) {
                                    Toast.makeText(ChatActivity.this, "You have blocked this person. So you cannot chat with this user.", Toast.LENGTH_LONG).show();
                                }

                                if (otherBlockedYou) {
                                    Toast.makeText(ChatActivity.this, "This person have blocked you. So you cannot chat with this user.", Toast.LENGTH_LONG).show();
                                }
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

    @Override
    protected void onResume() {
        super.onResume();

        getBlockStatus();
    }

    private void initViews() {

        userOnlineText = findViewById(R.id.lastSeenText);
        userOnlineText.setVisibility(View.GONE);

        headerLayoutOnTop = findViewById(R.id.headerLayout);
        selectionOptionsLayoutCard = findViewById(R.id.selectionOptionsLayout);
        forwardIconCard = findViewById(R.id.forwardIcon);
        deleteIconCard = findViewById(R.id.deleteIcon);
        copyIconCard = findViewById(R.id.copyIcon);
        replyIcon = findViewById(R.id.replyIcon);
        replyLayoutChat = findViewById(R.id.replyLayoutChat);
        replySenderNameViewChat = findViewById(R.id.senderNameReplyChat);
        replyMessageTextViewChat = findViewById(R.id.messageTextReplyChat);
        replyLayoutChat.setVisibility(View.GONE);

        forwardIconCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //selectionOptionsLayoutCard.setVisibility(View.GONE);
                if (listForSelectedModels.size() == 0) {
                    Toast.makeText(ChatActivity.this, "Select a Message to Forward", Toast.LENGTH_SHORT).show();
                    return;
                }

                HelperClass.forwardModelListToForwardMessages.addAll(new ArrayList<>(listForSelectedModels));
                startActivity(new Intent(ChatActivity.this, ForwardMessagesActivity.class));

                disableSelectionMode();
            }
        });

        replyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String senderIdOfRepyMessageSenderId = listForSelectedModels.get(0).getSenderId();
                if (senderIdOfRepyMessageSenderId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    replyMessageSenderName = "" + HelperClass.decrypt(myName);
                    replySenderNameViewChat.setText("You");
                } else {
                    replyMessageSenderName = "" + receiverName;
                }

                String typeOfReplyMessage = listForSelectedModels.get(0).getMessageType();

                if (typeOfReplyMessage.equals("img")) {
                    replyMessageText = "Image";
                    replyMessageImageLink = listForSelectedModels.get(0).getMessage();
                } else if (typeOfReplyMessage.equals("video")) {
                    replyMessageText = "Video";
                    replyMessageImageLink = listForSelectedModels.get(0).getMessage();
                } else if (typeOfReplyMessage.equals("text")) {
                    replyMessageText = listForSelectedModels.get(0).getMessage();
                } else if (typeOfReplyMessage.equals("doc") || typeOfReplyMessage.equals("pptx") || typeOfReplyMessage.equals("ppt") || typeOfReplyMessage.equals("pdf") || typeOfReplyMessage.equals("xlsx") || typeOfReplyMessage.equals("xls") || typeOfReplyMessage.equals("docx")) {
                    replyMessageText = "Document";
                } else if (typeOfReplyMessage.equals("audio")) {
                    replyMessageText = "Audio";
                }

                replyMessageSelectedTime = listForSelectedModels.get(0).getMessageKey();

                //Toast.makeText(ChatActivity.this, ": " + receiverName, Toast.LENGTH_SHORT).show();
                replyLayoutChat.setVisibility(View.VISIBLE);
                replyMessageTextViewChat.setText(replyMessageText);

                disableSelectionMode();
            }
        });

        findViewById(R.id.crossBtnReplyLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replyMessageSenderName = "";
                replyMessageText = "";
                replyMessageImageLink = "";
                replyMessageSelectedTime = "";
                replyLayoutChat.setVisibility(View.GONE);
            }
        });

        deleteIconCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (listForSelectedModels.size() == 0) {
                    Toast.makeText(ChatActivity.this, "Select a Message to Delete", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] colors = {"Delete For Me", "Delete For Everyone"};
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("");

                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            for (ForwardModel mm: listForSelectedModels) {
                                deleteMessageClicked(mm, false);
                            }
                        } else if (which == 1) {
                            for (ForwardModel mm: listForSelectedModels) {
                                deleteMessageClicked(mm, true);
                            }
                        }

                        disableSelectionMode();
                    }
                });
                builder.show();
            }
        });

        copyIconCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (listForSelectedModels.size() == 0) {
                    Toast.makeText(ChatActivity.this, "Select a Message to Copy", Toast.LENGTH_SHORT).show();
                    return;
                }

                String messageToCopyToClipboard = "";

                for(ForwardModel mm: listForSelectedModels) {
                    messageToCopyToClipboard = messageToCopyToClipboard + "\n" + mm.getMessage();
                }

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Message", messageToCopyToClipboard);
                clipboard.setPrimaryClip(clip);

                if (listForSelectedModels.size() == 1) {
                    Toast.makeText(ChatActivity.this, "Text Copied To Clipboard", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatActivity.this, "Messages Text Copied To Clipboard", Toast.LENGTH_SHORT).show();
                }

                disableSelectionMode();
            }
        });

        mainLayout = findViewById(R.id.mainLayoutChatScreen);
        friendRequestLayout = findViewById(R.id.friendRequestScreen);
        notFriendLayout = findViewById(R.id.noFriendLayout);

        Database_Reference = FirebaseDatabase.getInstance().getReference("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(receiverId);
        mDatabaseReferenceOfFriendToAddMessage = FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverId).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        final String[] emojis_array = getResources().getStringArray(R.array.emojisArray);

        emojisList = new ArrayList<>();

        for (String s : emojis_array) {
            emojisList.add(s);
        }

        messageEdit = findViewById(R.id.edit_message_chat);
        sendBtn = findViewById(R.id.send_btn_chat);

        attachBtn = findViewById(R.id.attach_btn_chat);
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isMediaBlockedByOtherPerson) {
                    MDToast.makeText(ChatActivity.this, "Sorry, You cannot send media to this person", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();
                    return;
                } else {
                    attachDialog.show();
                }
            }
        });
        messagesRecycler = findViewById(R.id.chat_recycler);
        aviChat = findViewById(R.id.avi);
        userNameText = findViewById(R.id.userNameChat);
        findViewById(R.id.back_btn_).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        userImageView = findViewById(R.id.userImageChat);

        userNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this, UserProfileActivity.class).putExtra("userId", receiverId));
            }
        });

        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this, UserProfileActivity.class).putExtra("userId", receiverId));
            }
        });

        userNameText.setText(receiverName);

        try {
            Glide.with(ChatActivity.this).load(receiverImage).into(userImageView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        messagesRecycler = findViewById(R.id.chat_recycler);
        chatAdapter = new ChatAdapter(messageArrayList, this);
        messagesRecycler.setAdapter(chatAdapter);
        LinearLayoutManager ll = new LinearLayoutManager(this);
        ll.setStackFromEnd(true);
        messagesRecycler.setLayoutManager(ll);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageEdit.getText().toString().isEmpty()) {

                } else {

                    if (sendNow){
                        String message = HelperClass.encrypt(messageEdit.getText().toString());

                        String key = String.valueOf(System.currentTimeMillis());

                        ChatModel modelToSend = new ChatModel(message, HelperClass.encrypt(myName), FirebaseAuth.getInstance().getCurrentUser().getUid(), HelperClass.encrypt(receiverName), receiverId, String.valueOf(System.currentTimeMillis()), key, "text", false, false, "sent", "0", "0", autoDestructionStatus, autoDestructionTime);

                        if (!replyMessageText.equals("")) {
                            modelToSend.setReplySenderMessageName(replyMessageSenderName);
                            modelToSend.setReplyMessageImageLink(replyMessageImageLink);
                            modelToSend.setReplyMessageText(replyMessageText);
                            modelToSend.setReplyMessageSelectedTime(replyMessageSelectedTime);
                        }
                        replyLayoutChat.setVisibility(View.GONE);

                        Database_Reference.child(key).setValue(modelToSend);//storing actual msg with name of the user
                        mDatabaseReferenceOfFriendToAddMessage.child(key).setValue(modelToSend);
                        messageEdit.setText("");

                        if (modelToSend.getAutoDestructionStatus()) {
                            autoDestroyMessageTimer(modelToSend);
                        }

                        if (!otherBusyModeOn) {
                            Send_Notification(HelperClass.decrypt(message));
                        }

                        if (otherAutoReplyModeOn) {
                            if (isFirstMessage) {
                                sendAutoReply();
                            }
                        }

                    } else {

                        final Calendar currentDate = Calendar.getInstance();
                        Calendar date = Calendar.getInstance();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(ChatActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                date.set(year, monthOfYear, dayOfMonth);
                                TimePickerDialog timePickerDialog = new TimePickerDialog(ChatActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        date.set(Calendar.MINUTE, minute);
                                        date.set(Calendar.SECOND, 0);

                                        if (date.getTimeInMillis() < System.currentTimeMillis()){
                                            Log.v("DateTime__", "The choosen one " + date.getTimeInMillis());
                                            MDToast.makeText(ChatActivity.this, "Please Select a time after current time", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();
                                        } else {
                                            MDToast.makeText(ChatActivity.this, "Date Time Selected. The Message will be sent on the picked Time and Date", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
                                            Log.v("DateTime__", "The choosen one " + date.getTimeInMillis());


                                            String message = HelperClass.encrypt(messageEdit.getText().toString());

                                            String key = String.valueOf(System.currentTimeMillis());

                                            ChatModel modelToSend = new ChatModel(message, HelperClass.encrypt(myName), FirebaseAuth.getInstance().getCurrentUser().getUid(), HelperClass.encrypt(receiverName), receiverId, String.valueOf(date.getTimeInMillis()), key, "text", false, false, "sent", "0", "0", autoDestructionStatus, autoDestructionTime);

                                            if (!replyMessageText.equals("")) {
                                                modelToSend.setReplySenderMessageName(replyMessageSenderName);
                                                modelToSend.setReplyMessageImageLink(replyMessageImageLink);
                                                modelToSend.setReplyMessageText(replyMessageText);
                                                modelToSend.setReplyMessageSelectedTime(replyMessageSelectedTime);
                                            }

                                            replyLayoutChat.setVisibility(View.GONE);

                                            Database_Reference.child(key).setValue(modelToSend);//storing actual msg with name of the user
                                            mDatabaseReferenceOfFriendToAddMessage.child(key).setValue(modelToSend);
                                            messageEdit.setText("");
                                            //Send Other Type of Notification
                                            sendScheduledNotification(HelperClass.decrypt(message), modelToSend);
                                        }
                                    }
                                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false);
                                timePickerDialog.show();
                            }
                        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE));
                        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                        datePickerDialog.show();

                    }
                }
            }
        });

        sendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                String[] colors = {"Send Now", "Scheduled Sending"};
//
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Sending Method");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which == 0) {
                            sendNow = true;
                            sendBtn.setImageDrawable(getResources().getDrawable(R.drawable.send_icc));

                        } else if (which == 1) {
                            sendNow = false;
                            sendBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_access_time_clock_24));
                        }
                    }
                });
                builder.show();

                return false;
            }
        });
    }

    private void autoDestroyMessageTimer(ChatModel model) {
        try {
            HelperClass.deleteMessageWithTimer(model, MainActivity.getMainActivity(), receiverId);
        } catch (Exception e) {
            e.printStackTrace();

            try {
                HelperClass.deleteMessageWithTimer(model, GroupChatActivity.getGroupChatActivity(), receiverId);
            } catch (Exception ee) { }
        }
    }

    private void sendAutoReply(){
        Thread timer = new Thread(){
            public void run() {
                try {
                    sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long time = System.currentTimeMillis();
                            ChatModel modelForAutoReply = new ChatModel(HelperClass.encrypt(otherAutoReplyMessage), HelperClass.encrypt(receiverName), receiverId, HelperClass.encrypt(myName), FirebaseAuth.getInstance().getCurrentUser().getUid(), String.valueOf(time), String.valueOf(time), "text", false, false, "sent", "0", "0", receiverAutoDestructionStatus, receiverAutoDestructionTime);
                            Database_Reference.child(String.valueOf(time)).setValue(modelForAutoReply);
                            mDatabaseReferenceOfFriendToAddMessage.child(String.valueOf(time)).setValue(modelForAutoReply);

                            if (modelForAutoReply.getAutoDestructionStatus()) {
                                autoDestroyMessageTimer(modelForAutoReply);
                            }
                        }
                    });
                }
            }
        };
        timer.start();
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    ScheduledExecutorService lastSeenManageScheduler;
    private void manageLastSeen(boolean isLastSeenEnabled) {
        if (isLastSeenEnabled) {
            Log.v("lastSeenManaging__", "Last Seen Show Not Blocked");
            lastSeenManageScheduler.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    Log.i("hello", "world");
                    Log.v("lastSeenManaging__", "Running");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            FirebaseDatabase.getInstance().getReference().child("users").child(receiverId).child("lastSeen")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (isLastSeenEnabled) {
                                        userOnlineText.setVisibility(View.VISIBLE);
                                    } else {
                                        userOnlineText.setVisibility(View.GONE);
                                    }

                                    if (dataSnapshot.exists()) {

                                        try {
                                            String lastSeenTime = dataSnapshot.getValue(String.class);

                                            long lastSeenLong = Long.parseLong(lastSeenTime);
                                            long currentSeen = System.currentTimeMillis();

                                            long seconds = (currentSeen - lastSeenLong) / 1000;

                                            if (seconds > 30) {

                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTimeInMillis(lastSeenLong);

                                                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                                int minute = calendar.get(Calendar.MINUTE);

                                                String hou = "";

                                                String min = "";
                                                if (minute < 10) {
                                                    min = "0" + minute;
                                                } else {
                                                    min = String.valueOf(minute);
                                                }

                                                String format = "";
                                                if (hour == 0) {
                                                    hour += 12;
                                                    format = "AM";
                                                } else if (hour == 12) {
                                                    format = "PM";
                                                } else if (hour > 12) {
                                                    hour -= 12;
                                                    format = "PM";
                                                } else {
                                                    format = "AM";
                                                }

                                                if (hour < 10) {
                                                    hou = "0" + hour;
                                                } else {
                                                    hou = String.valueOf(hour);
                                                }

                                                int month = calendar.get(Calendar.MONTH);
                                                month++;

                                                String time = "Last Seen at " + hou + ":" + min + " " + format + ", " + getTimeAgo(lastSeenLong);

                                                userOnlineText.setText(time);

                                            } else {
                                                userOnlineText.setText("Online");
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        userOnlineText.setText("Last Seen Not Available");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                }
            }, 1, 10, TimeUnit.SECONDS);
        } else {
            Log.v("lastSeenManaging__", "Last Seen Show Blocked");
            try {
                lastSeenManageScheduler.shutdownNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getTimeAgo(long time) {
        //if timestamp given in seconds, convert to millis time *= 1000; }
        long now = System.currentTimeMillis();
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeInMillis(System.currentTimeMillis());
        Calendar timeCal = Calendar.getInstance();
        timeCal.setTimeInMillis(time);
        if (time > now || time <= 0) {
            return null;
        }

        long diff = now - time;

        boolean isToday = DateUtils.isToday(time);

        if (isToday){
            return  "Today";
        } else {
            if ((nowCal.get(Calendar.DATE) - 1) == (timeCal.get(Calendar.DATE))){
                return "Yesterday";
            } else {

                long dayss = diff /(long) DAY_MILLIS;

                if (dayss < 30) {
                    return diff / DAY_MILLIS + " days ago";
                } else {

                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                    return dateFormat.format(new Date(time));
                }
            }
        }
    }

    private void insertAfterScheduledTime(ChatModel modelToAddAfterScheduuledTime) {
        long timeToAddModel = Long.parseLong(modelToAddAfterScheduuledTime.getTimestamp()) - System.currentTimeMillis();

        Thread timer = new Thread(){
            public void run() {
                try {
                    sleep(timeToAddModel);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messageArrayList.add(modelToAddAfterScheduuledTime);
                            Collections.sort(messageArrayList, new Comparator<ChatModel>() {
                                @Override
                                public int compare(ChatModel chatModel, ChatModel t1) {
                                    return chatModel.getTimestamp().compareTo(t1.getTimestamp());
                                }
                            });

                            aviChat.setVisibility(View.GONE);
                            chatAdapter.notifyDataSetChanged();

                            messagesRecycler.scrollToPosition(messageArrayList.size() - 1);
                        }
                    });
                }
            }
        };
        timer.start();
    }

    List<ChatModel> messageArrayList = new ArrayList<>();
    private void getChatMessages() {

//        Database_Reference.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot d, @Nullable String s) {
//                try {
//                    String message = HelperClass.decrypt(d.child("message").getValue(String.class));
//                    String messageKey = d.child("messageKey").getValue(String.class);
//                    String messageType = d.child("messageType").getValue(String.class);
//                    String receiver_id = d.child("receiver_id").getValue(String.class);
//                    String receiver_name = HelperClass.decrypt(d.child("receiver_name").getValue(String.class));
//                    String sender_id = d.child("sender_id").getValue(String.class);
//                    String sender_name = HelperClass.decrypt(d.child("sender_name").getValue(String.class));
//                    String timestamp = d.child("timestamp").getValue(String.class);
//
//                    String messageStatus = "sent";
//
//                    if (d.hasChild("messageStatus")) {
//                        messageStatus = d.child("messageStatus").getValue(String.class);
//                    }
//
//                    boolean deletedByReceiver = d.child("deletedByReceiver").getValue(Boolean.class);
//                    boolean deletedBySender = d.child("deletedBySender").getValue(Boolean.class);
//
//                    String deliveredTime = "0", readTime = "0";
//
//                    if (d.hasChild("deliveredTime")) {
//                        deliveredTime = d.child("deliveredTime").getValue(String.class);
//                    }
//
//                    if (d.hasChild("readTime")) {
//                        readTime = d.child("readTime").getValue(String.class);
//                    }
//
//                    boolean autoDestructionStatusOfMessage = false;
//                    String autoDestructionTimeOfMessage = "0";
//
//                    if (d.hasChild("autoDestructionStatus")) {
//                        autoDestructionStatusOfMessage = d.child("autoDestructionStatus").getValue(Boolean.class);
//                    }
//
//                    if (d.hasChild("autoDestructionTime")) {
//                        autoDestructionTimeOfMessage = d.child("autoDestructionTime").getValue(String.class);
//                    }
//
//                    boolean isAutoDeletionTimePassed = false;
//
//                    if (autoDestructionStatusOfMessage) {
//
//                        if (!autoDestructionTimeOfMessage.equals("")) {
//                            long timeTillDestruction = Long.parseLong(timestamp) + Long.parseLong(autoDestructionTimeOfMessage);
//
//                            if (timeTillDestruction < System.currentTimeMillis()) {
//                                isAutoDeletionTimePassed = true;
//                            }
//                        }
//                    }
//
//                    //ChatMessageModel model = d.getValue(ChatMessageModel.class);
//
//                    if (!isAutoDeletionTimePassed) {
//                        ChatModel mod = new ChatModel(message, sender_name, sender_id, receiver_name, receiver_id, timestamp, messageKey, messageType, deletedByReceiver, deletedBySender, messageStatus, deliveredTime, readTime, autoDestructionStatusOfMessage, autoDestructionTimeOfMessage);
//
//                        if (Long.parseLong(timestamp) < System.currentTimeMillis()) {
//                            messageArrayList.add(mod);
//                        } else {
//                            insertAfterScheduledTime(mod);
//                        }
//                    } else {
//                        deleteTheMessage(messageKey);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                Collections.sort(messageArrayList, new Comparator<ChatModel>() {
//                    @Override
//                    public int compare(ChatModel chatModel, ChatModel t1) {
//                        return chatModel.getTimestamp().compareTo(t1.getTimestamp());
//                    }
//                });
//
//                aviChat.setVisibility(View.GONE);
//                chatAdapter.notifyDataSetChanged();
//
//                messagesRecycler.scrollToPosition(messageArrayList.size() - 1);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                Log.v("ValueChanged__", "Value: " + dataSnapshot);
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot d) {
//                Log.v("ValueRemoved__", "Value: " + d);
//
//                String messageKey = d.child("messageKey").getValue(String.class);
//
//                int indexOfDeletedModel = 0;
//
//                for (int i = 0; i < messageArrayList.size(); i++) {
//                    try {
//                        ChatModel mm = messageArrayList.get(i);
//
//                        if (mm.getMessageKey().equals(messageKey)) {
//                            indexOfDeletedModel = i;
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                try {
//                    messageArrayList.remove(indexOfDeletedModel);
//                    chatAdapter.notifyItemRemoved(indexOfDeletedModel);
//
//                    aviChat.setVisibility(View.GONE);
//                    chatAdapter.notifyDataSetChanged();
////
//                    messagesRecycler.scrollToPosition(messageArrayList.size() - 1);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        Database_Reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageArrayList.clear();

                ChatModel headerModel = new ChatModel("", "", "", "", "", "", "", "headerType", false, false, "", "", "", false, "");

                messageArrayList.add(headerModel);

                for (DataSnapshot d : dataSnapshot.getChildren()) {

                    try {
                    String message = HelperClass.decrypt(d.child("message").getValue(String.class));
                    String messageKey = d.child("messageKey").getValue(String.class);
                    String messageType = d.child("messageType").getValue(String.class);
                    String receiver_id = d.child("receiver_id").getValue(String.class);
                    String receiver_name = HelperClass.decrypt(d.child("receiver_name").getValue(String.class));
                    String sender_id = d.child("sender_id").getValue(String.class);
                    String sender_name = HelperClass.decrypt(d.child("sender_name").getValue(String.class));
                    String timestamp = d.child("timestamp").getValue(String.class);

                    String messageStatus = "sent";

                    if (d.hasChild("messageStatus")) {
                        messageStatus = d.child("messageStatus").getValue(String.class);
                    }

                    boolean deletedByReceiver = d.child("deletedByReceiver").getValue(Boolean.class);
                    boolean deletedBySender = d.child("deletedBySender").getValue(Boolean.class);

                    String deliveredTime = "0", readTime = "0";

                    if (d.hasChild("deliveredTime")) {
                        deliveredTime = d.child("deliveredTime").getValue(String.class);
                    }

                    if (d.hasChild("readTime")) {
                        readTime = d.child("readTime").getValue(String.class);
                    }

                    boolean autoDestructionStatusOfMessage = false;
                    String autoDestructionTimeOfMessage = "0";

                    if (d.hasChild("autoDestructionStatus")) {
                        autoDestructionStatusOfMessage = d.child("autoDestructionStatus").getValue(Boolean.class);
                    }

                    if (d.hasChild("autoDestructionTime")) {
                        autoDestructionTimeOfMessage = d.child("autoDestructionTime").getValue(String.class);
                    }

                    boolean isAutoDeletionTimePassed = false;

                    if (autoDestructionStatusOfMessage) {

                        if (!autoDestructionTimeOfMessage.equals("")) {
                            long timeTillDestruction = Long.parseLong(timestamp) + Long.parseLong(autoDestructionTimeOfMessage);

                            if (timeTillDestruction < System.currentTimeMillis()) {
                                isAutoDeletionTimePassed = true;
                            }
                        }
                    }

                    //ChatMessageModel model = d.getValue(ChatMessageModel.class);

                    if (!isAutoDeletionTimePassed) {
                        ChatModel mod = new ChatModel(message, sender_name, sender_id, receiver_name, receiver_id, timestamp, messageKey, messageType, deletedByReceiver, deletedBySender, messageStatus, deliveredTime, readTime, autoDestructionStatusOfMessage, autoDestructionTimeOfMessage);

                        if (d.hasChild("replySenderMessageName")) {
                            if (!d.child("replySenderMessageName").getValue(String.class).isEmpty()) {
                                mod.setReplySenderMessageName(d.child("replySenderMessageName").getValue(String.class));
                                mod.setReplyMessageText(d.child("replyMessageText").getValue(String.class));
                                mod.setReplyMessageImageLink(d.child("replyMessageImageLink").getValue(String.class));
                                mod.setReplyMessageSelectedTime(d.child("replyMessageSelectedTime").getValue(String.class));
                            }
                        }

                        if (Long.parseLong(timestamp) < System.currentTimeMillis()) {
                            messageArrayList.add(mod);
                        } else {
                            insertAfterScheduledTime(mod);
                        }
                    } else {
                        deleteTheMessage(messageKey);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                }
                Collections.sort(messageArrayList, new Comparator<ChatModel>() {
                    @Override
                    public int compare(ChatModel chatModel, ChatModel t1) {
                        return chatModel.getTimestamp().compareTo(t1.getTimestamp());
                    }
                });

                aviChat.setVisibility(View.GONE);
                chatAdapter.notifyDataSetChanged();

                for (int iii = 0; iii < messageArrayList.size(); iii++) {

                    ChatModel mmmsd = messageArrayList.get(iii);

                    Log.v("Modelsdf___", "ModelPos: " + mmmsd.getMessageType() + "  pos: " + iii);
                }

                messagesRecycler.scrollToPosition(messageArrayList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteTheMessage(String key) {
        DatabaseReference mDatabaseForDeletingMessageFromHis = FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key);
        DatabaseReference mDatabaseForDeletingMessageFromMy =  FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(receiverId).child(key);

        try {
            mDatabaseForDeletingMessageFromHis.removeValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mDatabaseForDeletingMessageFromMy.removeValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    MediaPlayer mp = new MediaPlayer();

    private void disableSelectionMode(){
        if (isSelectionModeOn) {
            isSelectionModeOn = false;
            selectionOptionsLayoutCard.setVisibility(View.GONE);
            listForSelectedModels.clear();
            chatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        if (isSelectionModeOn) {
            disableSelectionMode();
        } else {
            super.onBackPressed();
        }

        if (mp.isPlaying()) {
            mp.stop();
            mp.reset();
        }
    }

    private void playAudio(File file) {
        try {
            mp.setDataSource(file.getPath());
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Log.d("", "onRequestPermissionsResult: permission failed");
                    return;
                }
            }
//            pickingDocument();
        }

        if (requestCode == 1012) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Log.d("", "onRequestPermissionsResult: permission failed");
                    return;
                }
            }
        }
    }

    long messageSendingTime = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        messageSendingTime = 0;

        if (resultCode == RESULT_OK){

            String[] colors = {"Send Now", "Send Later"};
//
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder.setTitle("Sending Method");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (which == 0) {
                        messageSendingTime = 0;

                        afterActivityResults(requestCode, resultCode, data);

                    } else if (which == 1) {

                        final Calendar currentDate = Calendar.getInstance();
                        Calendar date = Calendar.getInstance();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(ChatActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                date.set(year, monthOfYear, dayOfMonth);
                                TimePickerDialog timePickerDialog = new TimePickerDialog(ChatActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        date.set(Calendar.MINUTE, minute);
                                        date.set(Calendar.SECOND, 0);

                                        if (date.getTimeInMillis() < System.currentTimeMillis()){
                                            Log.v("DateTime__", "The choosen one " + date.getTimeInMillis());
                                            MDToast.makeText(ChatActivity.this, "Please Select a time after current time", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();
                                        } else {
                                            MDToast.makeText(ChatActivity.this, "Date Time Selected. The Message will be sent on the picked Time and Date", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();

                                            messageSendingTime = date.getTimeInMillis();

                                            Log.v("DateTime__", "The choosen one " + date.getTimeInMillis());

                                            afterActivityResults(requestCode, resultCode, data);
                                            //Send Other Type of Notification
                                        }
                                    }
                                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false);
                                timePickerDialog.show();
                            }
                        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE));
                        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                        datePickerDialog.show();
                    }
                }
            });
            builder.show();
        }
    }

    private void afterActivityResults(int requestCode, int resultCode, Intent data){
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                Bitmap bm = null;
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //groupImage.setImageBitmap(bitmap);

                replyLayoutChat.setVisibility(View.GONE);
                uploadImage(resultUri);
                MDToast.makeText(ChatActivity.this, "Please Wait While we Upload your image", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }//images picking done
        else if (requestCode == FilePickerConst.REQUEST_CODE_DOC) {
            //Means that this is a document file
            if (resultCode == Activity.RESULT_OK && data != null) {
                List<String> docPaths = new ArrayList<>();
                docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
            }

        } else if (requestCode == 1231) {

            //Measn that this is a audio file

            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();

                String ss = uri.toString();

                replyLayoutChat.setVisibility(View.GONE);
                uploadmp3(uri);
                MDToast.makeText(ChatActivity.this, "Please Wait While we Upload your Audio", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
            }


        } else if (requestCode == 1122) {
            //Recorded Audio
            if (resultCode == RESULT_OK) {
                Uri uri = Uri.fromFile(new File(data.getStringExtra("savePath")));
                String ss = uri.toString();
                replyLayoutChat.setVisibility(View.GONE);
                uploadmp3(uri);
                MDToast.makeText(ChatActivity.this, "Please Wait While we Upload your Audio Clip", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
            }
        } else if (requestCode == 1321) {
            //Means that this is a video file

            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();

                String ss = uri.toString();

                replyLayoutChat.setVisibility(View.GONE);
                uploadVideo(uri);
                MDToast.makeText(ChatActivity.this, "Please Wait While we Upload your Video", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
            }
        } else if (requestCode == 1232) {
            if (resultCode == RESULT_OK) {
                //Means this is a document file
                Uri uri = data.getData();
                String path = "";
                path = uri.toString().substring(uri.toString().lastIndexOf(".") + 1);
                MDToast.makeText(ChatActivity.this, "Please Wait While we Upload your Document", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();

                replyLayoutChat.setVisibility(View.GONE);
                uploadDocument(uri, path);
            }
        } else if (requestCode == 1921){
            try {
                Uri selectedImage = data.getData();
                replyLayoutChat.setVisibility(View.GONE);
                uploadImage(selectedImage);
                MDToast.makeText(ChatActivity.this, "Please Wait While we Upload your image", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1922){
            try {
                Uri[] results = {Uri.fromFile(new File(mCameraFileName))};
                Uri selectedUri = results[0];
                replyLayoutChat.setVisibility(View.GONE);
                uploadImage(selectedUri);
                MDToast.makeText(ChatActivity.this, "Please Wait While we Upload your image", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    long messageSendingTimeInMillisBeforePushing = 0;

    private void uploadImage(Uri resultUri) {
        try {
            aviChat.setVisibility(View.VISIBLE);

            final String key = String.valueOf(System.currentTimeMillis());

            final StorageReference Submit_Datareference = FirebaseStorage.getInstance().getReference("ChatImages").child(key);

            Submit_Datareference.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Submit_Datareference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String Image_Download_Link = uri.toString();

                            Image_Download_Link = HelperClass.encrypt(Image_Download_Link);

                            if (messageSendingTime == 0){
                                messageSendingTimeInMillisBeforePushing = System.currentTimeMillis();
                            } else {
                                messageSendingTimeInMillisBeforePushing = messageSendingTime;
                            }

                            ChatModel model = new ChatModel(Image_Download_Link, HelperClass.encrypt(myName), FirebaseAuth.getInstance().getCurrentUser().getUid(), HelperClass.encrypt(receiverName), receiverId, String.valueOf(messageSendingTimeInMillisBeforePushing), key, "img", false, false, "sent", "0", "0", autoDestructionStatus, autoDestructionTime);

                            if (!replyMessageText.equals("")) {
                                model.setReplySenderMessageName(replyMessageSenderName);
                                model.setReplyMessageImageLink(replyMessageImageLink);
                                model.setReplyMessageText(replyMessageText);
                                model.setReplyMessageSelectedTime(replyMessageSelectedTime);
                            }

                            replyLayoutChat.setVisibility(View.GONE);

                            Database_Reference.child(key).setValue(model);//storing actual msg with name of the user
                            mDatabaseReferenceOfFriendToAddMessage.child(key).setValue(model);//storing actual msg with name of the user
                            aviChat.setVisibility(View.GONE);

                            if (model.getAutoDestructionStatus()) {
                                autoDestroyMessageTimer(model);
                            }

                            if (messageSendingTime == 0){
                                if (!otherBusyModeOn) {
                                    Send_Notification("Image");
                                }

                                if (otherAutoReplyModeOn) {
                                    if (isFirstMessage) {
                                        sendAutoReply();
//                                        long time = Long.parseLong(key) + 121;
//
//                                        ChatModel modelForAutoReply = new ChatModel(otherAutoReplyMessage, receiverName, receiverId, myName, FirebaseAuth.getInstance().getCurrentUser().getUid(), String.valueOf(time), String.valueOf(time), "text", false, false, "sent");
//                                        Database_Reference.child(String.valueOf(time)).setValue(modelForAutoReply);
//                                        mDatabaseReferenceOfFriendToAddMessage.child(String.valueOf(time)).setValue(modelForAutoReply);
                                    }
                                }

                            } else {
                                sendScheduledNotification("Image", model);
                            }
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadDocument(Uri resultUri, final String format) {
        try {

            aviChat.setVisibility(View.VISIBLE);

            final String key = String.valueOf(System.currentTimeMillis());

            final StorageReference Submit_Datareference = FirebaseStorage.getInstance().getReference("ChatDocs").child(key);

            Submit_Datareference.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Submit_Datareference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String Image_Download_Link = uri.toString();
                            Image_Download_Link = HelperClass.encrypt(Image_Download_Link);

                            if (messageSendingTime == 0){
                                messageSendingTimeInMillisBeforePushing = System.currentTimeMillis();
                            } else {
                                messageSendingTimeInMillisBeforePushing = messageSendingTime;
                            }

                            ChatModel model = new ChatModel(Image_Download_Link, HelperClass.encrypt(myName), FirebaseAuth.getInstance().getCurrentUser().getUid(), HelperClass.encrypt(receiverName), receiverId, String.valueOf(messageSendingTimeInMillisBeforePushing), key, format, false, false, "sent", "0", "0", autoDestructionStatus, autoDestructionTime);

                            if (!replyMessageText.equals("")) {
                                model.setReplySenderMessageName(replyMessageSenderName);
                                model.setReplyMessageImageLink(replyMessageImageLink);
                                model.setReplyMessageText(replyMessageText);
                                model.setReplyMessageSelectedTime(replyMessageSelectedTime);
                            }

                            Database_Reference.child(key).setValue(model);//storing actual msg with name of the user
                            mDatabaseReferenceOfFriendToAddMessage.child(key).setValue(model);//storing actual msg with name of the user
                            aviChat.setVisibility(View.GONE);

                            if (model.getAutoDestructionStatus()) {
                                autoDestroyMessageTimer(model);
                            }

                            if (messageSendingTime == 0){

                                if (!otherBusyModeOn) {
                                    Send_Notification("Document");
                                }

                                if (otherAutoReplyModeOn) {
                                    if (isFirstMessage) {
                                        sendAutoReply();
//                                        long time = Long.parseLong(key) + 121;
//
//                                        ChatModel modelForAutoReply = new ChatModel(otherAutoReplyMessage, receiverName, receiverId, myName, FirebaseAuth.getInstance().getCurrentUser().getUid(), String.valueOf(time), String.valueOf(time), "text", false, false, "sent");
//                                        Database_Reference.child(String.valueOf(time)).setValue(modelForAutoReply);
//                                        mDatabaseReferenceOfFriendToAddMessage.child(String.valueOf(time)).setValue(modelForAutoReply);
                                    }
                                }

                            } else {
                                sendScheduledNotification("Document", model);
                            }
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadmp3(Uri resultUri) {
        try {
            aviChat.setVisibility(View.VISIBLE);

            final String key = String.valueOf(System.currentTimeMillis());

            final StorageReference Submit_Datareference = FirebaseStorage.getInstance().getReference("ChatAudios").child(key);

            Submit_Datareference.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Submit_Datareference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String Image_Download_Link = uri.toString();
                            Image_Download_Link = HelperClass.encrypt(Image_Download_Link);

                            if (messageSendingTime == 0){
                                messageSendingTimeInMillisBeforePushing = System.currentTimeMillis();
                            } else {
                                messageSendingTimeInMillisBeforePushing = messageSendingTime;
                            }

                            ChatModel model = new ChatModel(Image_Download_Link, HelperClass.encrypt(myName), FirebaseAuth.getInstance().getCurrentUser().getUid(), HelperClass.encrypt(receiverName), receiverId, String.valueOf(messageSendingTimeInMillisBeforePushing), key, "audio", false, false, "sent", "0", "0", autoDestructionStatus, autoDestructionTime);

                            if (!replyMessageText.equals("")) {
                                model.setReplySenderMessageName(replyMessageSenderName);
                                model.setReplyMessageImageLink(replyMessageImageLink);
                                model.setReplyMessageText(replyMessageText);
                                model.setReplyMessageSelectedTime(replyMessageSelectedTime);
                            }

                            Database_Reference.child(key).setValue(model);//storing actual msg with name of the user
                            mDatabaseReferenceOfFriendToAddMessage.child(key).setValue(model);//storing actual msg with name of the user
                            aviChat.setVisibility(View.GONE);

                            if (model.getAutoDestructionStatus()) {
                                autoDestroyMessageTimer(model);
                            }

                            if (messageSendingTime == 0) {

                                if (!otherBusyModeOn){
                                    Send_Notification("Audio");
                                }

                                if (otherAutoReplyModeOn) {
                                    if (isFirstMessage) {
                                        sendAutoReply();
//                                        long time = Long.parseLong(key) + 121;
//
//                                        ChatModel modelForAutoReply = new ChatModel(otherAutoReplyMessage, receiverName, receiverId, myName, FirebaseAuth.getInstance().getCurrentUser().getUid(), String.valueOf(time), String.valueOf(time), "text", false, false, "sent");
//                                        Database_Reference.child(String.valueOf(time)).setValue(modelForAutoReply);
//                                        mDatabaseReferenceOfFriendToAddMessage.child(String.valueOf(time)).setValue(modelForAutoReply);
                                    }
                                }

                            } else {
                                sendScheduledNotification("Audio", model);
                            }

                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    aviChat.setVisibility(View.GONE);
                    e.printStackTrace();
                    Toast.makeText(ChatActivity.this, "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadVideo(Uri resultUri) {
        try {
            aviChat.setVisibility(View.VISIBLE);

            final String key = String.valueOf(System.currentTimeMillis());

            final StorageReference Submit_Datareference = FirebaseStorage.getInstance().getReference("ChatVideos").child(key);

            Submit_Datareference.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Submit_Datareference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String Image_Download_Link = uri.toString();
                            Image_Download_Link = HelperClass.encrypt(Image_Download_Link);

                            if (messageSendingTime == 0){
                                messageSendingTimeInMillisBeforePushing = System.currentTimeMillis();
                            } else {
                                messageSendingTimeInMillisBeforePushing = messageSendingTime;
                            }

                            ChatModel model = new ChatModel(Image_Download_Link, HelperClass.encrypt(myName), FirebaseAuth.getInstance().getCurrentUser().getUid(), HelperClass.encrypt(receiverName), receiverId, String.valueOf(messageSendingTimeInMillisBeforePushing), key, "video", false, false, "sent", "0", "0", autoDestructionStatus, autoDestructionTime);

                            if (!replyMessageText.equals("")) {
                                model.setReplySenderMessageName(replyMessageSenderName);
                                model.setReplyMessageImageLink(replyMessageImageLink);
                                model.setReplyMessageText(replyMessageText);
                                model.setReplyMessageSelectedTime(replyMessageSelectedTime);
                            }

                            Database_Reference.child(key).setValue(model);//storing actual msg with name of the user
                            mDatabaseReferenceOfFriendToAddMessage.child(key).setValue(model);//storing actual msg with name of the user
                            aviChat.setVisibility(View.GONE);

                            if (model.getAutoDestructionStatus()) {
                                autoDestroyMessageTimer(model);
                            }

                            if (messageSendingTime == 0){
                                if (!otherBusyModeOn){
                                    Send_Notification("Video");
                                }

                                if (otherAutoReplyModeOn) {
                                    if (isFirstMessage) {
                                        sendAutoReply();
//                                        long time = Long.parseLong(key) + 121;
//
//                                        ChatModel modelForAutoReply = new ChatModel(otherAutoReplyMessage, receiverName, receiverId, myName, FirebaseAuth.getInstance().getCurrentUser().getUid(), String.valueOf(time), String.valueOf(time), "text", false, false, "sent");
//                                        Database_Reference.child(String.valueOf(time)).setValue(modelForAutoReply);
//                                        mDatabaseReferenceOfFriendToAddMessage.child(String.valueOf(time)).setValue(modelForAutoReply);
                                    }
                                }

                            } else {
                                sendScheduledNotification("Video", model);
                            }
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Send_Notification(String message) {

        try {
            MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.tick_sound);
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mPlayer.reset();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        DatabaseReference Notification_Reference;
        try {
            Notification_Reference = FirebaseDatabase.getInstance().getReference("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(myName).child(receiverId).child(receiverName).child(message);
        } catch (Exception e) {
            e.printStackTrace();
            Notification_Reference = FirebaseDatabase.getInstance().getReference("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(myName).child(receiverId).child(receiverName).child("TextMessage");
        }

        String Notif_Id = Notification_Reference.push().getKey();
        HashMap Notif_map = new HashMap();
        Notif_map.put("To", receiverId);
        Notif_map.put("From", FirebaseAuth.getInstance().getCurrentUser().getUid());
        Notif_map.put("Type", "Message");
        Notification_Reference.child(Notif_Id).updateChildren(Notif_map);
    }

    private void sendScheduledNotification(String message, ChatModel model){
        HelperClass.sendNotificationForPersonelChat(ChatActivity.this, otherPersonToken, model, message, HelperClass.decrypt(myImage));
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

    private void deleteMessageClicked(ForwardModel model, boolean deleteForEveryone) {

        if (deleteForEveryone){
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(ChatActivity.receiverId);
            mDatabase.child(model.getMessageKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){
                        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Chats").child(ChatActivity.receiverId).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        mDatabase2.child(model.getMessageKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task2) {
                                if (task2.isSuccessful()){
                                    //Toast.makeText(context, "Message Deleted For Everyone", Toast.LENGTH_SHORT).show();
                                } else {
                                    //Toast.makeText(context, "Error Deleting On Other Side: " + task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        //Toast.makeText(context, "Error Deleting On Your Side: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(ChatActivity.receiverId);
            mDatabase.child(model.getMessageKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        //Toast.makeText(ChatActivity.this, "Message Deleted For You", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
