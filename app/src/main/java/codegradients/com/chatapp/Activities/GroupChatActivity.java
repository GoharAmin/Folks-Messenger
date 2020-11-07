package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.valdesekamdem.library.mdtoast.MDToast;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import codegradients.com.chatapp.Models.ForwardModel;
import codegradients.com.chatapp.Models.GroupMemberModelForToken;
import codegradients.com.chatapp.helper_classes.HelperClass;
import de.hdodenhof.circleimageview.CircleImageView;
import codegradients.com.chatapp.Models.ContactModel;
import codegradients.com.chatapp.Models.GroupMessageModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.GroupChatAdapter;

public class GroupChatActivity extends AppCompatActivity {

    public static List<ForwardModel> listForSelectedModels = new ArrayList<>();
    public static boolean isSelectionModeOn = false;
    public static RelativeLayout selectionOptionsLayoutCard;
    public static ImageView forwardIconCard, deleteIconCard, copyIconCard, replyIcon;

    String groupName = "";
    String groupId = "";
    String groupImage = "";

    TextView groupNameText, groupMembersHeadingText;
    CircleImageView groupImageView;

    EditText messageEdit;
    FloatingActionButton sendBtn;
    ImageView attachBtn, memojiIcon;

    RecyclerView messagesRecycler;
    GroupChatAdapter messagesAdapter;
    List<GroupMessageModel> messagesArrayList = new ArrayList<>();
    AVLoadingIndicatorView aviImageSending;

    //Firebase Instances
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseForMessages;
    DatabaseReference mDatabaseForGettingMyInformation;

    BottomSheetDialog attachDialog, emojisDialog;

    ProgressDialog downloadProgressDialog;

    String mCameraFileName = "";

    boolean sendNow = true;

    List<String> memojisList = new ArrayList<>();

    public String replyMessageSenderName = "", replyMessageText = "", replyMessageImageLink = "", replyMessageSelectedTime = "";
    RelativeLayout replyLayoutChat;
    TextView replySenderNameViewChat, replyMessageTextViewChat;

    private void showEmojiDialog() {
        emojisDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.emoji_dialog_layout, null);

        class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {

            @NonNull
            @Override
            public EmojiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(GroupChatActivity.this).inflate(R.layout.item_emoji_dialog, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull EmojiAdapter.ViewHolder holder, int position) {
                Glide.with(GroupChatActivity.this).
                        load(memojisList.get(position)).
                        apply(new RequestOptions().placeholder(R.drawable.progress_animation).error(R.drawable.ic_baseline_error_outline_24)).
                        into(holder.emojiIconImage);

                holder.emojiIconImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(ChatActivity.this, "" + memojisList.get(position), Toast.LENGTH_SHORT).show();
                        sendEmojiIcon(memojisList.get(position));
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
        emojisRecycler.setLayoutManager(new GridLayoutManager(GroupChatActivity.this, 4));

        emojisDialog.setContentView(sheetView);
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

    private void sendEmojiIcon(String Image_Download_Link) {
        final String key = String.valueOf(System.currentTimeMillis());

        GroupMessageModel model = new GroupMessageModel(HelperClass.encrypt(Image_Download_Link), groupId, mAuth.getCurrentUser().getUid(), key, HelperClass.encrypt(myName), String.valueOf(System.currentTimeMillis()), "img");

        if (!replyMessageText.equals("")) {
            model.setReplySenderMessageName(replyMessageSenderName);
            model.setReplyMessageImageLink(replyMessageImageLink);
            model.setReplyMessageText(replyMessageText);
            model.setReplyMessageSelectedTime(replyMessageSelectedTime);
        }

        replyLayoutChat.setVisibility(View.GONE);

        mDatabaseForMessages.child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                aviImageSending.setVisibility(View.GONE);
                messageEdit.setText("");

                if (messageSendingTime == 0){
                    Send_Notification("Image");
                } else {
                    sendScheduledNotification("Image", model);
                }
            }
        });
    }

    private void showAttachDialog(){

        attachDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.sending_options_layout, null);

        LinearLayout imageLayout = sheetView.findViewById(R.id.image_type_selected);
        LinearLayout videoLayout = sheetView.findViewById(R.id.vid_type_selected);
        LinearLayout audioLayout = sheetView.findViewById(R.id.audio_type_selected);
        LinearLayout docLayout = sheetView.findViewById(R.id.docuemnt_type_selected);
        sheetView.findViewById(R.id.linearLayout_recordAudoi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatActivity.this,RecordAudioActivity.class);
                intent.putExtra(RecordAudioActivity.KEY_ACTIVITY_TYPE,"gc");
                startActivityForResult(intent,1122);
                attachDialog.dismiss();
            }
        });
        imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                CropImage.activity()
//                        .setAspectRatio(1, 1)
//                        .start(GroupChatActivity.this);

                String[] colors = {"Camera", "Gallery"};
//
                android.app.AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
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
                                Toast.makeText(GroupChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else if (which == 1){
                            try {
                                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(pickPhoto , 1921);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(GroupChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                if (ActivityCompat.checkSelfPermission(GroupChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GroupChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
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
                //showAlertDialog();
                if (ActivityCompat.checkSelfPermission(GroupChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GroupChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
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
                if (ActivityCompat.checkSelfPermission(GroupChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GroupChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
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
    public static String myName = "";
    private void getMyName(){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("userName")){
                    myName = dataSnapshot.child("userName").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //setting and getting Group Chat activity
    private static GroupChatActivity groupChatActivity;

    public static GroupChatActivity getGroupChatActivity() {
        return groupChatActivity;
    }

    private static void setGroupChatActivity(GroupChatActivity groupChatActivity) {
        GroupChatActivity.groupChatActivity = groupChatActivity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mAuth = FirebaseAuth.getInstance();

        GroupChatActivity.setGroupChatActivity(this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        showAttachDialog();
        showEmojiDialog();
        downloadProgressDialog = new ProgressDialog(this);
        downloadProgressDialog.setTitle("Downloading");
        downloadProgressDialog.setMessage("Downloading File For The First Time");
        downloadProgressDialog.setCancelable(false);
        if (ActivityCompat.checkSelfPermission(GroupChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(GroupChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1012);
        }

        groupName = getIntent().getStringExtra("grpName");
        groupId = getIntent().getStringExtra("grpId");

        getMyName();

        findViewById(R.id.back_btn_).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        messagesRecycler = findViewById(R.id.chat_recycler);
        messagesAdapter = new GroupChatAdapter(messagesArrayList, this);
        messagesRecycler.setAdapter(messagesAdapter);
        LinearLayoutManager ll = new LinearLayoutManager(this);
        ll.setStackFromEnd(true);
        messagesRecycler.setLayoutManager(ll);

        attachBtn = findViewById(R.id.attach_btn_chat);
        memojiIcon = findViewById(R.id.emojiIconGChat);
        groupNameText = findViewById(R.id.groupName_chat);
        groupMembersHeadingText = findViewById(R.id.groupMembersNamesHeading);
        groupMembersHeadingText.setVisibility(View.GONE);
        groupNameText.setText(groupName);
        groupImageView = findViewById(R.id.groupImage_chat);
        messageEdit = findViewById(R.id.edit_message_chat);
        sendBtn = findViewById(R.id.send_btn_chat);
        replyIcon = findViewById(R.id.replyIcon);
        replyLayoutChat = findViewById(R.id.replyLayoutChat);
        replySenderNameViewChat = findViewById(R.id.senderNameReplyChat);
        replyMessageTextViewChat = findViewById(R.id.messageTextReplyChat);
        replyLayoutChat.setVisibility(View.GONE);


        selectionOptionsLayoutCard = findViewById(R.id.selectionOptionsLayout);
        forwardIconCard = findViewById(R.id.forwardIcon);
        deleteIconCard = findViewById(R.id.deleteIcon);
        copyIconCard = findViewById(R.id.copyIcon);

        replyIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String senderIdOfRepyMessageSenderId = listForSelectedModels.get(0).getSenderId();
                if (senderIdOfRepyMessageSenderId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    replyMessageSenderName = "" + HelperClass.decrypt(myName);
                } else {

                    String nameOfSenderReplyMessage = "";
                    for(GroupMemberModelForToken mmmm: groupMemberModelForTokenList) {
                        if (mmmm.getPersonId().equals(senderIdOfRepyMessageSenderId)) {
                            nameOfSenderReplyMessage = mmmm.getPersonName();
                        }
                    }

                    replyMessageSenderName = "" + HelperClass.decrypt(nameOfSenderReplyMessage);
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
                replySenderNameViewChat.setText(replyMessageSenderName);
                replyMessageTextViewChat.setText(replyMessageText);

                disableSelectionMode();
            }
        });

        forwardIconCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //selectionOptionsLayoutCard.setVisibility(View.GONE);
                if (listForSelectedModels.size() == 0) {
                    Toast.makeText(GroupChatActivity.this, "Select a Message to Forward", Toast.LENGTH_SHORT).show();
                    return;
                }

                HelperClass.forwardModelListToForwardMessages.addAll(new ArrayList<>(listForSelectedModels));
                startActivity(new Intent(GroupChatActivity.this, ForwardMessagesActivity.class));

                disableSelectionMode();
            }
        });

        deleteIconCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String[] colors = {"Delete For Me", "Delete For Everyone"};
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
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

                String messageToCopyToClipboard = "";

                for(ForwardModel mm: listForSelectedModels) {
                    messageToCopyToClipboard = messageToCopyToClipboard + "\n" + mm.getMessage();
                }

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Message", messageToCopyToClipboard);
                clipboard.setPrimaryClip(clip);

                if (listForSelectedModels.size() == 1) {
                    Toast.makeText(GroupChatActivity.this, "Text Copied To Clipboard", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GroupChatActivity.this, "Messages Text Copied To Clipboard", Toast.LENGTH_SHORT).show();
                }

                disableSelectionMode();
            }
        });

        aviImageSending = findViewById(R.id.avi_chat_image_sending);
        aviImageSending.setVisibility(View.GONE);

        findViewById(R.id.infoIconGroupChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GroupChatActivity.this, GroupDetailActivity.class).putExtra("Id", groupId).putExtra("Name", groupName));
            }
        });

        groupImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupChatActivity.this, GroupDetailActivity.class).putExtra("Id", groupId).putExtra("Name", groupName));
            }
        });

        findViewById(R.id.nameAndMembersLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GroupChatActivity.this, GroupDetailActivity.class).putExtra("Id", groupId).putExtra("Name", groupName));
            }
        });

//        groupNameText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(GroupChatActivity.this, GroupDetailActivity.class).putExtra("Id", groupId).putExtra("Name", groupName));
//            }
//        });
        if (groupId != null) {
            mDatabaseForMessages = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("Messages");
            getMessages();
            gettingMyInfo();
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!messageEdit.getText().toString().isEmpty()) {

                    if (sendNow){
                        final String messag = HelperClass.encrypt(messageEdit.getText().toString());

                        String key = String.valueOf(System.currentTimeMillis());
                        GroupMessageModel model = new GroupMessageModel(messag, groupId, mAuth.getCurrentUser().getUid(), key, HelperClass.encrypt(myName), String.valueOf(System.currentTimeMillis()), "text");

                        if (!replyMessageText.equals("")) {
                            model.setReplySenderMessageName(replyMessageSenderName);
                            model.setReplyMessageImageLink(replyMessageImageLink);
                            model.setReplyMessageText(replyMessageText);
                            model.setReplyMessageSelectedTime(replyMessageSelectedTime);
                        }

                        replyLayoutChat.setVisibility(View.GONE);

                        mDatabaseForMessages.child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                messageEdit.setText("");
                                Send_Notification(HelperClass.decrypt(messag));
                            }
                        });
                    } else {
                        final Calendar currentDate = Calendar.getInstance();
                        Calendar date = Calendar.getInstance();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(GroupChatActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                date.set(year, monthOfYear, dayOfMonth);
                                TimePickerDialog timePickerDialog = new TimePickerDialog(GroupChatActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        date.set(Calendar.MINUTE, minute);

                                        if (date.getTimeInMillis() < System.currentTimeMillis()){
                                            Log.v("DateTime__", "The choosen one " + date.getTimeInMillis());
                                            MDToast.makeText(GroupChatActivity.this, "Please Select a time after current time", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();
                                        } else {
                                            MDToast.makeText(GroupChatActivity.this, "Date Time Selected. The Message will be sent on the picked Time and Date", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
                                            Log.v("DateTime__", "The choosen one " + date.getTimeInMillis());


                                            final String messag = HelperClass.encrypt(messageEdit.getText().toString());
                                            messageEdit.setText("");
                                            String key = String.valueOf(System.currentTimeMillis());
                                            GroupMessageModel model = new GroupMessageModel(messag, groupId, mAuth.getCurrentUser().getUid(), key, HelperClass.encrypt(myName), String.valueOf(System.currentTimeMillis()), "text");

                                            if (!replyMessageText.equals("")) {
                                                model.setReplySenderMessageName(replyMessageSenderName);
                                                model.setReplyMessageImageLink(replyMessageImageLink);
                                                model.setReplyMessageText(replyMessageText);
                                                model.setReplyMessageSelectedTime(replyMessageSelectedTime);
                                            }

                                            replyLayoutChat.setVisibility(View.GONE);

                                            sendScheduledNotification(HelperClass.decrypt(messag), model);

                                            mDatabaseForMessages.child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });
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
            }
        });

        sendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                String[] colors = {"Send Now", "Scheduled Sending"};
//
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
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

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attachDialog.show();
            }
        });
        memojiIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emojisDialog.show();
            }
        });

        getGroupInfo();
        getMemojis();
    }

    private void deleteMessageClicked(ForwardModel model, boolean deleteForEveryone) {
        if (!deleteForEveryone) {
            FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("Messages").child(model.getMessageKey()).child("deletedBy").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);

        } else  {
            FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("Messages").child(model.getMessageKey()).removeValue();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (groupId != null) {
            Get_Group_Members();
        }
    }

    private void getMessages() {
        try {

            mDatabaseForMessages.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    messagesArrayList.clear();

                    messagesArrayList.add(new GroupMessageModel("message", groupId, "", "", "", "", "headerType"));

                    for (DataSnapshot d: dataSnapshot.getChildren()){

                        String from = d.child("from").getValue().toString();
                        String groupId = d.child("groupId").getValue().toString();
                        String key = d.child("key").getValue().toString();
                        String message = HelperClass.decrypt(d.child("message").getValue().toString());
                        String messageType = d.child("messageType").getValue().toString();
                        String senderName = HelperClass.decrypt(d.child("senderName").getValue().toString());
                        String timestamp = d.child("timestamp").getValue().toString();

                        senderName = HelperClass.decrypt(senderName);

                        Log.v("Sender__", "Name: " + senderName + "   Message: " + message);

                        boolean isDeletedByMe = false;
                        if (d.hasChild("deletedBy")){
                            if (d.child("deletedBy").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                if (d.child("deletedBy").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(Boolean.class)) {
                                    isDeletedByMe = true;
                                }
                            }
                        }

                        if (!isDeletedByMe) {
                            if (Long.parseLong(timestamp) < System.currentTimeMillis()){

                                GroupMessageModel mod = new GroupMessageModel(message, groupId, from, key, senderName, timestamp, messageType);

                                if (d.hasChild("replySenderMessageName")) {
                                    if (!d.child("replySenderMessageName").getValue(String.class).isEmpty()) {
                                        mod.setReplySenderMessageName(d.child("replySenderMessageName").getValue(String.class));
                                        mod.setReplyMessageText(d.child("replyMessageText").getValue(String.class));
                                        mod.setReplyMessageImageLink(d.child("replyMessageImageLink").getValue(String.class));
                                        mod.setReplyMessageSelectedTime(d.child("replyMessageSelectedTime").getValue(String.class));
                                    }
                                }

                                messagesArrayList.add(mod);
                            }
                        }
                       // GroupMessageModel model = d.getValue(GroupMessageModel.class);
                    }

                    messagesAdapter.notifyDataSetChanged();
                    messagesRecycler.scrollToPosition(messagesAdapter.getItemCount() - 1);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    List<String> Users_Keys = new ArrayList<>();
    List<String> allUsersOfGroup = new ArrayList<>();

    private void Get_Group_Members() {
        try {
            DatabaseReference GET_MEMBERS = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);
            GET_MEMBERS.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Users_Keys.clear();
                    allUsersOfGroup.clear();
                    for (DataSnapshot Child : dataSnapshot.child("users").getChildren()) {

                        if (!mAuth.getUid().equals(Child.getKey())) {
                            Users_Keys.add(Child.getKey());
                        }

                        allUsersOfGroup.add(Child.getKey());
                    }

                    getNamesOfGroupMembers();

                    Log.d("Called", "Called:---- " + Users_Keys.size());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<GroupMemberModelForToken> groupMemberModelForTokenList = new ArrayList<>();

    private void getNamesOfGroupMembers(){
        groupMemberModelForTokenList.clear();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                groupMemberModelForTokenList.clear();

                String allUserNames = "";

                int count = 0;

                for (int i = 0; i < allUsersOfGroup.size(); i++){
                    String s = allUsersOfGroup.get(i);
                    count++;

                    if (dataSnapshot.hasChild(s)){
                        if (dataSnapshot.child(s).hasChild("userName")){

                            String userName = HelperClass.decrypt(dataSnapshot.child(s).child("userName").getValue(String.class));

                            if (dataSnapshot.child(s).hasChild("Device_Token")){

                                groupMemberModelForTokenList.add(new GroupMemberModelForToken(s, userName, dataSnapshot.child(s).child("Device_Token").getValue(String.class)));
                            }
                            //if (count < 4){

                                if (count == allUsersOfGroup.size()){
                                    allUserNames = allUserNames + userName + "";
                                } else {
                                    allUserNames = allUserNames + userName + ", ";
                                }
                            //}
                        }
                    }
                }

                groupMembersHeadingText.setText(allUserNames);
                groupMembersHeadingText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    ContactModel myProfile;

    private void gettingMyInfo() {
        try {
            mDatabaseForGettingMyInformation = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
            mDatabaseForGettingMyInformation.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("userName")) {
                        String id = dataSnapshot.getKey();
                        String name = HelperClass.decrypt(dataSnapshot.child("userName").getValue(String.class));
                        String image = HelperClass.decrypt(dataSnapshot.child("profileImage").getValue(String.class));
                        String number = HelperClass.decrypt(dataSnapshot.child("mobileNumber").getValue(String.class));
                        String about = HelperClass.decrypt(dataSnapshot.child("about").getValue(String.class));
                        String userToken = "";

                        if (dataSnapshot.hasChild("Device_Token")) {
                            userToken = dataSnapshot.child("Device_Token").getValue(String.class);
                        }

                        myProfile = new ContactModel(id, name, number,image, about, userToken);
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

    private void getGroupInfo() {
        if (groupId != null) {
            DatabaseReference mDatabaseForImage = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId);
            mDatabaseForImage.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    groupImage = dataSnapshot.child("groupImage").getValue(String.class);
                    try {
                        Glide.with(GroupChatActivity.this).load(groupImage).into(groupImageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
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

        if (requestCode == 1012){
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Log.d("", "onRequestPermissionsResult: permission failed");
                    return;
                }
            }
        }
    }

    MediaPlayer mp = new MediaPlayer();

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
    public void onBackPressed() {
        if (isSelectionModeOn) {
            disableSelectionMode();
        } else {
            super.onBackPressed();
        }

        if (mp.isPlaying()){
            mp.stop();
            mp.reset();
        }

        finish();
    }

    long messageSendingTime = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        messageSendingTime = 0;

        if (resultCode == RESULT_OK){
            String[] colors = {"Send Now", "Send Later"};
//
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
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
                        DatePickerDialog datePickerDialog = new DatePickerDialog(GroupChatActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                date.set(year, monthOfYear, dayOfMonth);
                                TimePickerDialog timePickerDialog = new TimePickerDialog(GroupChatActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        date.set(Calendar.MINUTE, minute);

                                        if (date.getTimeInMillis() < System.currentTimeMillis()){
                                            Log.v("DateTime__", "The choosen one " + date.getTimeInMillis());
                                            MDToast.makeText(GroupChatActivity.this, "Please Select a time after current time", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();
                                        } else {
                                            MDToast.makeText(GroupChatActivity.this, "Date Time Selected. The Message will be sent on the picked Time and Date", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();

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

    private void afterActivityResults(int requestCode, int resultCode, Intent data) {

        replyLayoutChat.setVisibility(View.GONE);

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

                uploadImage(resultUri);

                MDToast.makeText(GroupChatActivity.this, "Please Wait While we Upload your image", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        } //here comes other things sending

        else if (requestCode == 1231) {
            //Measn that this is a audio file

            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();

                String ss = uri.toString();

                uploadmp3(uri);

                MDToast.makeText(GroupChatActivity.this, "Please Wait While we Upload your Audio Clip", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
            }


        } else if (requestCode == 1122) {
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra("savePath");
                Uri uri1 = Uri.fromFile(new File(path));
                uploadmp3(uri1);
                MDToast.makeText(GroupChatActivity.this, "Please Wait While we Upload your Audio Clip", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
            }
        }

        else if (requestCode == 1321) {
            //Means that this is a video file

            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();

                String ss = uri.toString();

                uploadVideo(uri);

                MDToast.makeText(GroupChatActivity.this, "Please Wait While we Upload your Video", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
            }
        } else if (requestCode == 1232) {
            if (resultCode == RESULT_OK) {

                Uri uri = data.getData();

                String path = "";
                path = uri.toString().substring(uri.toString().lastIndexOf(".") + 1);
                MDToast.makeText(GroupChatActivity.this, "Please Wait While we Upload your Document", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();

                uploadDocument(uri, path);
            }
        } else if (requestCode == 1921){
            try {
                Uri selectedImage = data.getData();
                uploadImage(selectedImage);
                MDToast.makeText(GroupChatActivity.this, "Please Wait While we Upload your image", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(GroupChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1922){
            try {
                Uri[] results = {Uri.fromFile(new File(mCameraFileName))};
                Uri selectedUri = results[0];
                uploadImage(selectedUri);
                MDToast.makeText(GroupChatActivity.this, "Please Wait While we Upload your image", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(GroupChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    long messageSendingTimeInMillisBeforePushing = 0;

    private void uploadDocument(Uri resultUri, final String format) {
        try {
            aviImageSending.setVisibility(View.VISIBLE);
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

                            GroupMessageModel model = new GroupMessageModel(Image_Download_Link, groupId, mAuth.getCurrentUser().getUid(), key, HelperClass.encrypt(myName), String.valueOf(messageSendingTimeInMillisBeforePushing), format);

                            if (!replyMessageText.equals("")) {
                                model.setReplySenderMessageName(replyMessageSenderName);
                                model.setReplyMessageImageLink(replyMessageImageLink);
                                model.setReplyMessageText(replyMessageText);
                                model.setReplyMessageSelectedTime(replyMessageSelectedTime);
                            }

                            replyLayoutChat.setVisibility(View.GONE);

                            mDatabaseForMessages.child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    aviImageSending.setVisibility(View.GONE);
                                    messageEdit.setText("");

                                    if (messageSendingTime == 0){
                                        Send_Notification("Document");
                                    } else {
                                        sendScheduledNotification("Document", model);
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

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GroupChatActivity.this, "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadmp3(Uri resultUri) {
        try {
            aviImageSending.setVisibility(View.VISIBLE);

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

                            GroupMessageModel model = new GroupMessageModel(Image_Download_Link, groupId, mAuth.getCurrentUser().getUid(), key, HelperClass.encrypt(myName), String.valueOf(messageSendingTimeInMillisBeforePushing), "audio");

                            if (!replyMessageText.equals("")) {
                                model.setReplySenderMessageName(replyMessageSenderName);
                                model.setReplyMessageImageLink(replyMessageImageLink);
                                model.setReplyMessageText(replyMessageText);
                                model.setReplyMessageSelectedTime(replyMessageSelectedTime);
                            }

                            replyLayoutChat.setVisibility(View.GONE);

                            mDatabaseForMessages.child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    aviImageSending.setVisibility(View.GONE);
                                    messageEdit.setText("");

                                    if (messageSendingTime == 0){
                                        Send_Notification("Audio");
                                    } else {
                                        sendScheduledNotification("Audio", model);
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

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GroupChatActivity.this, "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadVideo(Uri resultUri) {
        try {
            aviImageSending.setVisibility(View.VISIBLE);

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

                            GroupMessageModel model = new GroupMessageModel(Image_Download_Link, groupId, mAuth.getCurrentUser().getUid(), key, HelperClass.encrypt(myName), String.valueOf(messageSendingTimeInMillisBeforePushing), "video");

                            if (!replyMessageText.equals("")) {
                                model.setReplySenderMessageName(replyMessageSenderName);
                                model.setReplyMessageImageLink(replyMessageImageLink);
                                model.setReplyMessageText(replyMessageText);
                                model.setReplyMessageSelectedTime(replyMessageSelectedTime);
                            }

                            replyLayoutChat.setVisibility(View.GONE);

                            mDatabaseForMessages.child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    aviImageSending.setVisibility(View.GONE);
                                    messageEdit.setText("");

                                    if (messageSendingTime == 0){
                                        Send_Notification("Video");
                                    } else {
                                        sendScheduledNotification("Video", model);
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

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GroupChatActivity.this, "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadImage(Uri resultUri) {

        try {
            aviImageSending.setVisibility(View.VISIBLE);

            final String key = String.valueOf(System.currentTimeMillis());

            final StorageReference Submit_Datareference = FirebaseStorage.getInstance().getReference("GroupChatImages").child(key);

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

                            GroupMessageModel model = new GroupMessageModel(Image_Download_Link, groupId, mAuth.getCurrentUser().getUid(), key, HelperClass.encrypt(myName), String.valueOf(messageSendingTimeInMillisBeforePushing), "img");

                            if (!replyMessageText.equals("")) {
                                model.setReplySenderMessageName(replyMessageSenderName);
                                model.setReplyMessageImageLink(replyMessageImageLink);
                                model.setReplyMessageText(replyMessageText);
                                model.setReplyMessageSelectedTime(replyMessageSelectedTime);
                            }

                            replyLayoutChat.setVisibility(View.GONE);

                            mDatabaseForMessages.child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    aviImageSending.setVisibility(View.GONE);
                                    messageEdit.setText("");

                                    if (messageSendingTime == 0){
                                        Send_Notification("Image");
                                    } else {
                                        sendScheduledNotification("Image", model);
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

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(GroupChatActivity.this, "Error!" + e.getMessage(), Toast.LENGTH_LONG).show();
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

        for (String membersId : Users_Keys) {
            try {
                DatabaseReference Notification_Reference = FirebaseDatabase.getInstance().getReference("GroupNotifications").child(groupId).child(groupName).child(membersId).child(myName).child(message);
                String Notif_Id = Notification_Reference.push().getKey();
                HashMap Notif_map = new HashMap();
                Notif_map.put("To", membersId);
                Notif_map.put("From", FirebaseAuth.getInstance().getCurrentUser().getUid());
                Notif_map.put("Type", "GroupMessage");
                Log.v("GroupNotingSent__", "Data: " + Notif_map);
                Notification_Reference.child(Notif_Id).updateChildren(Notif_map);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void sendScheduledNotification(String message, GroupMessageModel model){
        for (GroupMemberModelForToken memberModel: groupMemberModelForTokenList){
            HelperClass.sendNotificationForGroupChat(GroupChatActivity.this, memberModel.getPersonToken(), model, message, groupName);
        }
    }

    private void disableSelectionMode(){
        if (isSelectionModeOn) {
            isSelectionModeOn = false;
            selectionOptionsLayoutCard.setVisibility(View.GONE);
            listForSelectedModels.clear();
            messagesAdapter.notifyDataSetChanged();
        }
    }

}
