package codegradients.com.chatapp.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import codegradients.com.chatapp.Activities.GroupChatActivity;
import codegradients.com.chatapp.Activities.ImageViewingActivity;
import codegradients.com.chatapp.Activities.VideoPlayingActivity;
import codegradients.com.chatapp.Models.ForwardModel;
import codegradients.com.chatapp.Models.GroupMessageModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.HelperClass;
import codegradients.com.chatapp.helper_classes.InputStreamVolleyRequest;
import codegradients.com.chatapp.helper_classes.volleyInstance;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder> {

    List<GroupMessageModel> list;
    Context context;

    ProgressDialog downloadProgressDialog;

    public GroupChatAdapter(List<GroupMessageModel> list, Context context) {
        this.list = list;
        this.context = context;

        downloadProgressDialog = new ProgressDialog(context);
        downloadProgressDialog.setTitle("Downloading");
        downloadProgressDialog.setMessage("Downloading For the First time");
        downloadProgressDialog.setCancelable(false);
    }

    @NonNull
    @Override
    public GroupChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_message_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatAdapter.ViewHolder holder, int position) {

        final GroupMessageModel model = list.get(position);

        //headerType
        if (model.getMessageType().equals("headerType")) {

            holder.myLayout.setVisibility(View.GONE);
            holder.otherLayout.setVisibility(View.GONE);
            holder.groupLeavingCardView.setVisibility(View.GONE);
            holder.headerTypeLayout.setVisibility(View.VISIBLE);

        } else {
            holder.headerTypeLayout.setVisibility(View.GONE);
            if (model.getMessageType().equals("groupLeaving")) {
                //Group Leaving Message
                holder.myLayout.setVisibility(View.GONE);
                holder.otherLayout.setVisibility(View.GONE);
                holder.groupLeavingCardView.setVisibility(View.VISIBLE);

                holder.groupLeavingMessage.setText(model.getSenderName() + " has Left the Group");

                Long timeStamp = Long.parseLong(model.getTimestamp());

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timeStamp);

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

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

                String minutee;

                if (minute < 10) {
                    minutee = String.valueOf(0) + String.valueOf(minute);
                } else {
                    minutee = String.valueOf(minute);
                }

                String messageTime = String.valueOf(hour) + ":" + minutee + " " + format;
                Calendar today = Calendar.getInstance();
                today.setTimeInMillis(System.currentTimeMillis());


                if (calendar.get(Calendar.DATE) == today.get(Calendar.DATE)) {
                    messageTime = messageTime;
                    holder.groupLeavingTime.setText(messageTime);
                } else if (calendar.get(Calendar.DATE) == (today.get(Calendar.DATE) - 1)) {
                    messageTime = messageTime + ", Yesterday";
                    holder.groupLeavingTime.setText(messageTime);
                } else {

                    int mon = calendar.get(Calendar.MONTH);
                    mon++;

                    messageTime = messageTime + ", " + calendar.get(Calendar.DATE) + "/" + mon + "/" + calendar.get(Calendar.YEAR);
                    holder.groupLeavingTime.setText(messageTime);
                }

            } else if (model.getMessageType().equals("groupJoining")){

                //Group Leaving Message
                holder.myLayout.setVisibility(View.GONE);
                holder.otherLayout.setVisibility(View.GONE);
                holder.groupLeavingCardView.setVisibility(View.VISIBLE);

                String messageToShow = "";

                if (model.getFrom().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    messageToShow = "You were added by " + model.getMessage();
                } else {
                    messageToShow = "" + model.getMessage() + " has added " + HelperClass.decrypt(model.getSenderName()) + " to the group.";
                }

                holder.groupLeavingMessage.setText(messageToShow);

                Long timeStamp = Long.parseLong(model.getTimestamp());

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timeStamp);

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

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

                String minutee;

                if (minute < 10) {
                    minutee = String.valueOf(0) + String.valueOf(minute);
                } else {
                    minutee = String.valueOf(minute);
                }

                String messageTime = String.valueOf(hour) + ":" + minutee + " " + format;
                Calendar today = Calendar.getInstance();
                today.setTimeInMillis(System.currentTimeMillis());


                if (calendar.get(Calendar.DATE) == today.get(Calendar.DATE)) {
                    messageTime = messageTime;
                    holder.groupLeavingTime.setText(messageTime);
                } else if (calendar.get(Calendar.DATE) == (today.get(Calendar.DATE) - 1)) {
                    messageTime = messageTime + ", Yesterday";
                    holder.groupLeavingTime.setText(messageTime);
                } else {

                    int mon = calendar.get(Calendar.MONTH);
                    mon++;

                    messageTime = messageTime + ", " + calendar.get(Calendar.DATE) + "/" + mon + "/" + calendar.get(Calendar.YEAR);
                    holder.groupLeavingTime.setText(messageTime);
                }

            } else {

                //Normal MY Message
                holder.groupLeavingCardView.setVisibility(View.GONE);
                if (model.getFrom().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    holder.myLayout.setVisibility(View.VISIBLE);
                    holder.otherLayout.setVisibility(View.GONE);

                    holder.rl_image_messageRowMy.setVisibility(View.GONE);

                    if (!model.getReplySenderMessageName().equals("")) {
                        holder.replyLayoutMy.setVisibility(View.VISIBLE);

                        holder.replyMessageSenderNameMy.setText(model.getReplySenderMessageName());
                        holder.replyMessageTextMy.setText(model.getReplyMessageText());

                        if (!model.getReplyMessageImageLink().equals("")) {
                            try {
                                Glide.with(context).load(model.getReplyMessageImageLink()).into(holder.replyMessageImageMy);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            holder.replyMessageImageMy.setVisibility(View.VISIBLE);
                        } else {
                            holder.replyMessageImageMy.setVisibility(View.GONE);
                        }


                    } else {
                        holder.replyLayoutMy.setVisibility(View.GONE);
                    }


                    if (model.getMessageType().equals("img") || model.getMessageType().equals("video")) {

                        if (model.getMessageType().equals("video")) {
                            holder.playBtnMy.setVisibility(View.VISIBLE);
                        } else {
                            holder.playBtnMy.setVisibility(View.GONE);
                        }

                        holder.playBtnMy.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                context.startActivity(new Intent(context, VideoPlayingActivity.class).putExtra("videoURL", model.getMessage()).putExtra("headingText", model.getSenderName()));
                            }
                        });

                        holder.audioLayoutMy.setVisibility(View.GONE);
                        holder.rl_image_messageRowMy.setVisibility(View.VISIBLE);
                        holder.aviMy.setVisibility(View.VISIBLE);
                        holder.docLayoutMy.setVisibility(View.GONE);
                        holder.message_imageMy.setVisibility(View.VISIBLE);
                        holder.Message_View_IdMy.setVisibility(View.GONE);
                        Glide.with(context).load(model.getMessage()).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                holder.aviMy.setVisibility(View.GONE);
                                holder.message_imageMy.setImageResource(R.drawable.ic_camera_alt_blue_24dp);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.aviMy.setVisibility(View.GONE);
                                return false;
                            }
                        }).into(holder.message_imageMy);

//                    if (model.getMessageType().equals("img")){
//
//                        holder.message_imageMy.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                context.startActivity(new Intent(context, ImageViewingActivity.class).putExtra("URL", model.getMessage()));
//                            }
//                        });
//                    }

                    } else if (model.getMessageType().equals("text")) {
                        holder.message_imageMy.setVisibility(View.GONE);
                        holder.docLayoutMy.setVisibility(View.GONE);
                        holder.audioLayoutMy.setVisibility(View.GONE);
                        holder.rl_image_messageRowMy.setVisibility(View.GONE);
                        holder.Message_View_IdMy.setVisibility(View.VISIBLE);

                        holder.Message_View_IdMy.setText(model.getMessage());

                        //holder.Message_View_IdMy.setText(model.getMessage());
                    } else if (model.getMessageType().equals("doc") || model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt") || model.getMessageType().equals("pdf") || model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls") || model.getMessageType().equals("docx")) {
                        holder.docTextMy.setText(model.getKey() + "." + model.getMessageType());
                        holder.rl_image_messageRowMy.setVisibility(View.GONE);
                        holder.message_imageMy.setVisibility(View.GONE);
                        holder.aviMy.setVisibility(View.GONE);
                        holder.audioLayoutMy.setVisibility(View.GONE);
                        holder.Message_View_IdMy.setVisibility(View.GONE);
                        holder.docLayoutMy.setVisibility(View.VISIBLE);

                        holder.docLayoutMy.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Document");
                                if (!root.exists())
                                    root.mkdirs();

                                File file = new File(root, model.getKey() + "." + model.getMessageType());
                                if (file.exists()) {
                                    //loadPdf(file);

                                    Uri path = Uri.fromFile(file);
                                    Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                                    pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    if (model.getMessageType().equals("doc") || model.getMessageType().equals("docx")){
                                        pdfOpenintent.setDataAndType(path, "application/msword");
                                    } else if (model.getMessageType().equals("pdf")){
                                        pdfOpenintent.setDataAndType(path, "application/pdf");
                                    } else if (model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt")){
                                        pdfOpenintent.setDataAndType(path, "application/vnd.ms-powerpoint");
                                    } else if (model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls")){
                                        pdfOpenintent.setDataAndType(path, "application/vnd.ms-excel");
                                    } else {
                                        pdfOpenintent.setDataAndType(path, "application/pdf");
                                    }

                                    try {
                                        context.startActivity(Intent.createChooser(pdfOpenintent, "Open file with"));
                                    } catch (ActivityNotFoundException e) {

                                    }
                                } else {
                                    downloadProgressDialog.show();
                                    InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, model.getMessage(), new Response.Listener<byte[]>() {
                                        @Override
                                        public void onResponse(byte[] response) {
                                            try {
                                                if (response != null) {
                                                    FileOutputStream outputStream;
                                                    String name = model.getKey() + "." + model.getMessageType();
                                                    outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
                                                    outputStream.write(response);
                                                    outputStream.close();

                                                    File Originalfile = new File(context.getFilesDir().getPath() + "/" + model.getKey() + "." + model.getMessageType());

                                                    File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Document");
                                                    if (!root.exists())
                                                        root.mkdirs();
                                                    File file = new File(root, model.getKey() + "." + model.getMessageType());

                                                    try {
                                                        FileUtils.copyFile(Originalfile, file);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    downloadProgressDialog.dismiss();
                                                    if (file.exists()) {
                                                        //loadPdf(file);

                                                        Uri path = Uri.fromFile(file);
                                                        Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                                                        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        if (model.getMessageType().equals("doc") || model.getMessageType().equals("docx")){
                                                            pdfOpenintent.setDataAndType(path, "application/msword");
                                                        } else if (model.getMessageType().equals("pdf")){
                                                            pdfOpenintent.setDataAndType(path, "application/pdf");
                                                        } else if (model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt")){
                                                            pdfOpenintent.setDataAndType(path, "application/vnd.ms-powerpoint");
                                                        } else if (model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls")){
                                                            pdfOpenintent.setDataAndType(path, "application/vnd.ms-excel");
                                                        } else {
                                                            pdfOpenintent.setDataAndType(path, "application/pdf");
                                                        }
                                                        try {
                                                            context.startActivity(Intent.createChooser(pdfOpenintent, "Open file with"));
                                                        } catch (ActivityNotFoundException e) {

                                                        }
                                                    } else {
                                                        downloadProgressDialog.dismiss();
                                                        Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    downloadProgressDialog.dismiss();
                                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (Exception e) {
                                                downloadProgressDialog.dismiss();
                                                Toast.makeText(context, "Something went wrong while creating file", Toast.LENGTH_SHORT).show();
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            downloadProgressDialog.dismiss();
                                            Toast.makeText(context, "An Error Occurred while Downloading", Toast.LENGTH_SHORT).show();
                                            error.printStackTrace();
                                        }
                                    });

                                    volleyInstance.getmInstance(context).addToRequestQueue(request);
                                }
                            }
                        });

                    } else if (model.getMessageType().equals("audio")) {
                        holder.audioTextMy.setText(model.getKey());
                        holder.rl_image_messageRowMy.setVisibility(View.GONE);
                        holder.message_imageMy.setVisibility(View.GONE);
                        holder.aviMy.setVisibility(View.GONE);
                        holder.Message_View_IdMy.setVisibility(View.GONE);
                        holder.docLayoutMy.setVisibility(View.GONE);

                        holder.audioLayoutMy.setVisibility(View.VISIBLE);
                        holder.playBtnAudioMy.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //downloading and playing audio

                                File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Audio");
                                if (!root.exists())
                                    root.mkdirs();

                                File file = new File(root, model.getKey() + ".mp3");
                                if (file.exists()) {
                                    //loadPdf(file);

                                    if (mp.isPlaying()) {
                                        mp.stop();
                                        mp.reset();
                                        holder.playBtnAudioMy.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle_filled_white_24dp));
                                        return;
                                    }

                                    playAudio(file);

                                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mediaPlayer) {
                                            mp.stop();
                                            mp.reset();
                                            holder.playBtnAudioMy.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle_filled_white_24dp));
                                        }
                                    });

                                    holder.playBtnAudioMy.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_circle_filled_white_24dp));
                                } else {
                                    downloadProgressDialog.show();
                                    InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, model.getMessage(), new Response.Listener<byte[]>() {
                                        @Override
                                        public void onResponse(byte[] response) {
                                            try {
                                                if (response != null) {
                                                    FileOutputStream outputStream;
                                                    String name = model.getKey() + ".mp3";
                                                    outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
                                                    outputStream.write(response);
                                                    outputStream.close();

                                                    File Originalfile = new File(context.getFilesDir().getPath() + "/" + model.getKey() + ".mp3");

                                                    File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Audio");
                                                    if (!root.exists())
                                                        root.mkdirs();
                                                    File file = new File(root, model.getKey() + ".mp3");

                                                    try {
                                                        FileUtils.copyFile(Originalfile, file);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    downloadProgressDialog.dismiss();
                                                    if (file.exists()) {
                                                        //loadPdf(file);

                                                        if (mp.isPlaying()) {
                                                            mp.stop();
                                                            mp.reset();
                                                            holder.playBtnAudioMy.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle_filled_white_24dp));
                                                            return;

                                                        }

                                                        playAudio(file);
                                                        holder.playBtnAudioMy.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_circle_filled_white_24dp));

                                                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                            @Override
                                                            public void onCompletion(MediaPlayer mediaPlayer) {
                                                                mp.stop();
                                                                mp.reset();
                                                                holder.playBtnAudioMy.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle_filled_white_24dp));
                                                            }
                                                        });

                                                    } else {
                                                        downloadProgressDialog.dismiss();
                                                        Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    downloadProgressDialog.dismiss();
                                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (Exception e) {
                                                downloadProgressDialog.dismiss();
                                                Toast.makeText(context, "Something went wrong while creating file", Toast.LENGTH_SHORT).show();
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            downloadProgressDialog.dismiss();
                                            Toast.makeText(context, "An Error Occurred while Downloading", Toast.LENGTH_SHORT).show();
                                            error.printStackTrace();
                                        }
                                    });

                                    volleyInstance.getmInstance(context).addToRequestQueue(request);
                                }
                            }
                        });

                    }


                    Long timeStamp = Long.parseLong(model.getTimestamp());

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timeStamp);

                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

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

                    String minutee;

                    if (minute < 10) {
                        minutee = String.valueOf(0) + String.valueOf(minute);
                    } else {
                        minutee = String.valueOf(minute);
                    }

                    String messageTime = String.valueOf(hour) + ":" + minutee + " " + format;
                    Calendar today = Calendar.getInstance();
                    today.setTimeInMillis(System.currentTimeMillis());


                    if (calendar.get(Calendar.DATE) == today.get(Calendar.DATE)) {
                        messageTime = messageTime;
                        holder.timeMy.setText(messageTime);
                    } else if (calendar.get(Calendar.DATE) == (today.get(Calendar.DATE) - 1)) {
                        messageTime = messageTime + ", Yesterday";
                        holder.timeMy.setText(messageTime);
                    } else {

                        int mon = calendar.get(Calendar.MONTH);
                        mon++;

                        messageTime = messageTime + ", " + calendar.get(Calendar.DATE) + "/" + mon + "/" + calendar.get(Calendar.YEAR);
                        holder.timeMy.setText(messageTime);
                    }
                } else {
                    //Means the message is from other person

                    if (!model.getReplySenderMessageName().equals("")) {
                        holder.replyLayoutOther.setVisibility(View.VISIBLE);

                        holder.replyMessageSenderNameOther.setText(model.getReplySenderMessageName());
                        holder.replyMessageTextOther.setText(model.getReplyMessageText());

                        if (!model.getReplyMessageImageLink().equals("")) {
                            try {
                                Glide.with(context).load(model.getReplyMessageImageLink()).into(holder.replyMessageImageOther);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            holder.replyMessageImageOther.setVisibility(View.VISIBLE);
                        } else {
                            holder.replyMessageImageOther.setVisibility(View.GONE);
                        }


                    } else {
                        holder.replyLayoutOther.setVisibility(View.GONE);
                    }

                    DatabaseReference mDatabaseForGettingOtherPersonImage = FirebaseDatabase.getInstance().getReference().child("users").child(model.getFrom()).child("profileImage");
                    mDatabaseForGettingOtherPersonImage.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                String imageLInkOtherPerson = HelperClass.decrypt(dataSnapshot.getValue(String.class));

                                Glide.with(context).load(imageLInkOtherPerson).listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                }).into(holder.otherPersonImageView);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    holder.myLayout.setVisibility(View.GONE);
                    holder.otherLayout.setVisibility(View.VISIBLE);

                    holder.sender_name.setVisibility(View.VISIBLE);

                    if (model.getMessageType().equals("img") || model.getMessageType().equals("video")) {

                        if (model.getMessageType().equals("video")) {
                            holder.playBtn.setVisibility(View.VISIBLE);
                        } else {
                            holder.playBtn.setVisibility(View.GONE);
                        }

                        holder.playBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                context.startActivity(new Intent(context, VideoPlayingActivity.class).putExtra("videoURL", model.getMessage()).putExtra("headingText", model.getSenderName()));

                                //downloading and playing audio

// image naming and path  to include sd card  appending name you choose for file
//                        File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Videos");
//                        if (!root.exists())
//                            root.mkdirs();
//
//                        File file = new File(root, model.getKey() + ".mp4");
//                        if (file.exists()) {
//                            //loadPdf(file);
//
//                            Uri fileUri = Uri.fromFile(file);
//                            Intent intent = new Intent(Intent.ACTION_VIEW);
//                            intent.setDataAndType(fileUri, "video/*");
//                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//DO NOT FORGET THIS EVER
//                            context.startActivity(intent);
//
//
//                        } else {
//                            downloadProgressDialog.show();
//                            InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, model.getMessage(), new Response.Listener<byte[]>() {
//                                @Override
//                                public void onResponse(byte[] response) {
//                                    try {
//                                        if (response != null) {
//
//                                            FileOutputStream outputStream;
//                                            String name = model.getKey() + ".mp4";
//                                            outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
//                                            outputStream.write(response);
//                                            outputStream.close();
//
//                                            File Originalfile = new File(context.getFilesDir().getPath() + "/" + model.getKey() + ".mp4");
//
//                                            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Videos");
//                                            if (!root.exists())
//                                                root.mkdirs();
//                                            File file = new File(root, model.getKey() + ".mp4");
//
//                                            try {
//                                                FileUtils.copyFile(Originalfile, file);
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//                                            downloadProgressDialog.dismiss();
//
//                                            if (file.exists()) {
//                                                //loadPdf(file);
//
//                                                Uri fileUri = Uri.fromFile(file);
//                                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                                                intent.setDataAndType(fileUri, "video/*");
//                                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//DO NOT FORGET THIS EVER
//                                                context.startActivity(intent);
//                                            } else {
//                                                downloadProgressDialog.dismiss();
//                                                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
//                                            }
//                                        } else {
//                                            downloadProgressDialog.dismiss();
//                                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
//                                        }
//                                    } catch (Exception e) {
//                                        downloadProgressDialog.dismiss();
//                                        Toast.makeText(context, "Something went wrong while creating file", Toast.LENGTH_SHORT).show();
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }, new Response.ErrorListener() {
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//                                    downloadProgressDialog.dismiss();
//                                    Toast.makeText(context, "An Error Occurred while Downloading", Toast.LENGTH_SHORT).show();
//                                    error.printStackTrace();
//                                }
//                            });
//
//                            volleyInstance.getmInstance(context).addToRequestQueue(request);
//                        }
                            }
                        });

                        holder.audioLayout.setVisibility(View.GONE);
                        holder.rl_image_messageRow.setVisibility(View.VISIBLE);
                        holder.avi.setVisibility(View.VISIBLE);
                        holder.message_image.setVisibility(View.VISIBLE);
                        holder.Message_View_Id.setVisibility(View.GONE);
                        holder.docLayout.setVisibility(View.GONE);

                        Glide.with(context).load(model.getMessage()).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                holder.avi.setVisibility(View.GONE);
                                holder.message_image.setImageResource(R.drawable.ic_camera_alt_blue_24dp);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.avi.setVisibility(View.GONE);
                                return false;
                            }
                        }).into(holder.message_image);

//                    if (model.getMessageType().equals("img")){
//                        holder.message_image.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                context.startActivity(new Intent(context, ImageViewingActivity.class).putExtra("URL", model.getMessage()));
//                            }
//                        });
//                    }

                    } else if (model.getMessageType().equals("text")){
                        holder.message_image.setVisibility(View.GONE);
                        holder.rl_image_messageRow.setVisibility(View.GONE);
                        holder.Message_View_Id.setVisibility(View.VISIBLE);
                        holder.Message_View_Id.setText(model.getMessage());

                        //holder.Message_View_Id.setText(model.getMessage());
                        holder.docLayout.setVisibility(View.GONE);
                        holder.audioLayout.setVisibility(View.GONE);
                    } else if (model.getMessageType().equals("doc") || model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt") || model.getMessageType().equals("pdf") || model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls") || model.getMessageType().equals("docx")) {
                        holder.docText.setText(model.getKey() + "." + model.getMessageType());
                        holder.message_image.setVisibility(View.GONE);
                        holder.Message_View_Id.setVisibility(View.GONE);
                        holder.audioLayout.setVisibility(View.GONE);
                        holder.Message_View_Id.setText(model.getMessage());
                        holder.docLayout.setVisibility(View.VISIBLE);

                        holder.docLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Document");
                                if (!root.exists())
                                    root.mkdirs();

                                File file = new File(root, model.getKey() + "." + model.getMessageType());
                                if (file.exists()) {
                                    //loadPdf(file);

                                    Uri path = Uri.fromFile(file);
                                    Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                                    pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    if (model.getMessageType().equals("doc") || model.getMessageType().equals("docx")){
                                        pdfOpenintent.setDataAndType(path, "application/msword");
                                    } else if (model.getMessageType().equals("pdf")){
                                        pdfOpenintent.setDataAndType(path, "application/pdf");
                                    } else if (model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt")){
                                        pdfOpenintent.setDataAndType(path, "application/vnd.ms-powerpoint");
                                    } else if (model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls")){
                                        pdfOpenintent.setDataAndType(path, "application/vnd.ms-excel");
                                    } else {
                                        pdfOpenintent.setDataAndType(path, "application/pdf");
                                    }
                                    try {
                                        context.startActivity(Intent.createChooser(pdfOpenintent, "Open file with"));
                                    } catch (ActivityNotFoundException e) {

                                    }
                                } else {
                                    downloadProgressDialog.show();
                                    InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, model.getMessage(), new Response.Listener<byte[]>() {
                                        @Override
                                        public void onResponse(byte[] response) {
                                            try {
                                                if (response != null) {
                                                    FileOutputStream outputStream;
                                                    String name = model.getKey() + "." + model.getMessageType();
                                                    outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
                                                    outputStream.write(response);
                                                    outputStream.close();

                                                    File Originalfile = new File(context.getFilesDir().getPath() + "/" + model.getKey() + "." + model.getMessageType());

                                                    File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Document");
                                                    if (!root.exists())
                                                        root.mkdirs();
                                                    File file = new File(root, model.getKey() + "." + model.getMessageType());

                                                    try {
                                                        FileUtils.copyFile(Originalfile, file);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    downloadProgressDialog.dismiss();
                                                    if (file.exists()) {
                                                        //loadPdf(file);

                                                        Uri path = Uri.fromFile(file);
                                                        Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                                                        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        if (model.getMessageType().equals("doc") || model.getMessageType().equals("docx")){
                                                            pdfOpenintent.setDataAndType(path, "application/msword");
                                                        } else if (model.getMessageType().equals("pdf")){
                                                            pdfOpenintent.setDataAndType(path, "application/pdf");
                                                        } else if (model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt")){
                                                            pdfOpenintent.setDataAndType(path, "application/vnd.ms-powerpoint");
                                                        } else if (model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls")){
                                                            pdfOpenintent.setDataAndType(path, "application/vnd.ms-excel");
                                                        } else {
                                                            pdfOpenintent.setDataAndType(path, "application/pdf");
                                                        }
                                                        try {
                                                            context.startActivity(Intent.createChooser(pdfOpenintent, "Open file with"));
                                                        } catch (ActivityNotFoundException e) {

                                                        }
                                                    } else {
                                                        downloadProgressDialog.dismiss();
                                                        Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    downloadProgressDialog.dismiss();
                                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (Exception e) {
                                                downloadProgressDialog.dismiss();
                                                Toast.makeText(context, "Something went wrong while creating file", Toast.LENGTH_SHORT).show();
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            downloadProgressDialog.dismiss();
                                            Toast.makeText(context, "An Error Occurred while Downloading", Toast.LENGTH_SHORT).show();
                                            error.printStackTrace();
                                        }
                                    });

                                    volleyInstance.getmInstance(context).addToRequestQueue(request);
                                }
                            }
                        });
                    } else if (model.getMessageType().equals("audio")) {
                        holder.audioText.setText(model.getKey());
                        holder.message_image.setVisibility(View.GONE);
                        holder.Message_View_Id.setVisibility(View.GONE);
                        holder.Message_View_Id.setText(model.getMessage());
                        holder.docLayout.setVisibility(View.GONE);

                        holder.audioLayout.setVisibility(View.VISIBLE);

                        holder.playBtnAudio.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                //downloading and playing audio

                                File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Audio");
                                if (!root.exists())
                                    root.mkdirs();

                                File file = new File(root, model.getKey() + ".mp3");
                                if (file.exists()) {
                                    //loadPdf(file);

                                    if (mp.isPlaying()) {
                                        mp.stop();
                                        mp.reset();
                                        holder.playBtnAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle_filled_white_24dp));
                                        return;
                                    }

                                    playAudio(file);

                                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mediaPlayer) {
                                            mp.stop();
                                            mp.reset();
                                            holder.playBtnAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle_filled_white_24dp));
                                        }
                                    });

                                    holder.playBtnAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_circle_filled_white_24dp));
                                } else {
                                    downloadProgressDialog.show();
                                    InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, model.getMessage(), new Response.Listener<byte[]>() {
                                        @Override
                                        public void onResponse(byte[] response) {
                                            try {
                                                if (response != null) {

                                                    if (mp.isPlaying()) {
                                                        mp.stop();
                                                        mp.reset();
                                                        holder.playBtnAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle_filled_white_24dp));
                                                        return;

                                                    }

                                                    FileOutputStream outputStream;
                                                    String name = model.getKey() + ".mp3";
                                                    outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
                                                    outputStream.write(response);
                                                    outputStream.close();

                                                    File Originalfile = new File(context.getFilesDir().getPath() + "/" + model.getKey() + ".mp3");

                                                    File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Audio");
                                                    if (!root.exists())
                                                        root.mkdirs();
                                                    File file = new File(root, model.getKey() + ".mp3");

                                                    try {
                                                        FileUtils.copyFile(Originalfile, file);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                    downloadProgressDialog.dismiss();
                                                    if (file.exists()) {
                                                        //loadPdf(file);

                                                        if (mp.isPlaying()) {
                                                            mp.stop();
                                                            mp.reset();
                                                            holder.playBtnAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle_filled_white_24dp));
                                                            return;
                                                        }

                                                        playAudio(file);
                                                        holder.playBtnAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pause_circle_filled_white_24dp));

                                                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                            @Override
                                                            public void onCompletion(MediaPlayer mediaPlayer) {
                                                                mp.stop();
                                                                mp.reset();
                                                                holder.playBtnAudio.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle_filled_white_24dp));
                                                            }
                                                        });

                                                    } else {
                                                        downloadProgressDialog.dismiss();
                                                        Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    downloadProgressDialog.dismiss();
                                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            } catch (Exception e) {
                                                downloadProgressDialog.dismiss();
                                                Toast.makeText(context, "Something went wrong while creating file", Toast.LENGTH_SHORT).show();
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            downloadProgressDialog.dismiss();
                                            Toast.makeText(context, "An Error Occurred while Downloading", Toast.LENGTH_SHORT).show();
                                            error.printStackTrace();
                                        }
                                    });

                                    volleyInstance.getmInstance(context).addToRequestQueue(request);
                                }
                            }
                        });
                    }

                    holder.sender_name.setText(model.getSenderName());

                    long timeStamp = Long.parseLong(model.getTimestamp());

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timeStamp);

                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

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

                    String minutee;

                    if (minute < 10) {
                        minutee = String.valueOf(0) + String.valueOf(minute);
                    } else {
                        minutee = String.valueOf(minute);
                    }

                    String messageTime = String.valueOf(hour) + ":" + minutee + " " + format;

                    Calendar today = Calendar.getInstance();
                    today.setTimeInMillis(System.currentTimeMillis());

                    if (calendar.get(Calendar.DATE) == today.get(Calendar.DATE)) {
                        //messageTime = messageTime + ", Today";
                        messageTime = messageTime;
                        holder.time.setText(messageTime);
                    } else if (calendar.get(Calendar.DATE) == (today.get(Calendar.DATE) - 1)) {
                        messageTime = messageTime + ", Yesterday";
                        holder.time.setText(messageTime);
                    } else {

                        int mon = calendar.get(Calendar.MONTH);
                        mon ++;

                        messageTime = messageTime + ", " + calendar.get(Calendar.DATE) + "/" + mon + "/" + calendar.get(Calendar.YEAR);
                        holder.time.setText(messageTime);
                    }
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (model.getMessageType().equals("groupLeaving") || model.getMessageType().equals("groupJoining") || model.getMessageType().equals("headerType")) {
                            return;
                        }

                        if (holder.getAdapterPosition() == 0) {
                            return;
                        }

                        if (!GroupChatActivity.isSelectionModeOn) {
                            if (model.getMessageType().equals("img")){
                                context.startActivity(new Intent(context, ImageViewingActivity.class).putExtra("URL", model.getMessage()));
                            }
                        } else {
                            ForwardModel mm  = new ForwardModel(model.getMessageType(), model.getKey(), model.getMessage(), model.getFrom());

                            if (containsCheck(mm)){
                                //Toast.makeText(context, "Contains", Toast.LENGTH_SHORT).show();
                                removeModelFromSelectedList(mm);
                                holder.bgLayout.setBackgroundColor(Color.parseColor("#ffffff"));
                            } else {
                                //Toast.makeText(context, " Do Not Contains", Toast.LENGTH_SHORT).show();
                                addModelToSelectedList(mm);
                                holder.bgLayout.setBackgroundColor(Color.parseColor("#333B61F6"));
                            }
                        }
                    }
                });

                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        if (model.getMessageType().equals("groupLeaving") || model.getMessageType().equals("groupJoining") || model.getMessageType().equals("headerType")) {
                            return true;
                        }

                        if (holder.getAdapterPosition() == 0) {
                            return true;
                        }

                        if (GroupChatActivity.isSelectionModeOn) {
                            ForwardModel mm  = new ForwardModel(model.getMessageType(), model.getKey(), model.getMessage(), model.getFrom());

                            if (containsCheck(mm)){
                                //Toast.makeText(context, "Contains", Toast.LENGTH_SHORT).show();
                                removeModelFromSelectedList(mm);
                                holder.bgLayout.setBackgroundColor(Color.parseColor("#ffffff"));
                            } else {
                                //Toast.makeText(context, " Do Not Contains", Toast.LENGTH_SHORT).show();
                                addModelToSelectedList(mm);
                                holder.bgLayout.setBackgroundColor(Color.parseColor("#333B61F6"));
                            }
                        } else {
                            GroupChatActivity.isSelectionModeOn = true;
                            GroupChatActivity.selectionOptionsLayoutCard.setVisibility(View.VISIBLE);

                            ForwardModel mm  = new ForwardModel(model.getMessageType(), model.getKey(), model.getMessage(), model.getFrom());

                            if (containsCheck(mm)){
                                //Toast.makeText(context, "Contains", Toast.LENGTH_SHORT).show();
                                removeModelFromSelectedList(mm);
                                holder.bgLayout.setBackgroundColor(Color.parseColor("#ffffff"));
                            } else {
                                //Toast.makeText(context, " Do Not Contains", Toast.LENGTH_SHORT).show();
                                addModelToSelectedList(mm);
                                holder.bgLayout.setBackgroundColor(Color.parseColor("#333B61F6"));
                            }
                        }

//                List<String> options = new ArrayList<>();
//
//                if (model.getMessageType().equals("text")) {
//                    options.add("Copy To Clipboard");
//                }
//
//                if (model.getFrom().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                    //Toast.makeText(context, "My", Toast.LENGTH_SHORT).show();
//                    options.add("Forward");
//                    options.add("Delete");
//
//                } else {
//                    options.add("Forward");
//                }
//
//                String[] colors = new String[options.size()];
//                options.toArray(colors);
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle("");
//
//                builder.setItems(colors, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int position) {
//
//                        String textPosition = colors[position];
//                        switch (textPosition) {
//                            case "Delete":
//                                deleteMessageClicked(model);
//                                break;
//
//                            case "Copy To Clipboard":
//                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//                                ClipData clip = ClipData.newPlainText("Message", model.getMessage());
//                                clipboard.setPrimaryClip(clip);
//                                Toast.makeText(context, "Text Copied To Clipboard", Toast.LENGTH_SHORT).show();
//                                break;
//                        }
//                    }
//                });
//                builder.show();

                        return true;
                    }
                });
            }

            try {
                ForwardModel mm = new ForwardModel(model.getMessageType(), model.getKey(), model.getMessage(), model.getFrom());

                if (containsCheck(mm)) {
                    holder.bgLayout.setBackgroundColor(Color.parseColor("#333B61F6"));
                } else {
                    holder.bgLayout.setBackgroundColor(Color.parseColor("#ffffff"));
                }
            } catch (Exception e){}
        }
    }

    private void checkIfCopyToBeVisibleOrNot() {
        boolean oneMessageIsNotText = false;

        for (ForwardModel modelForward: GroupChatActivity.listForSelectedModels) {
            if (!modelForward.getMessageType().equals("text")) {
                oneMessageIsNotText = true;
            }
        }

        if (oneMessageIsNotText) {
            GroupChatActivity.copyIconCard.setVisibility(View.GONE);
        } else {
            GroupChatActivity.copyIconCard.setVisibility(View.VISIBLE);
        }

        boolean areAllMessagesByMe = true;

        for (ForwardModel modelForward: GroupChatActivity.listForSelectedModels) {
            if (!modelForward.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                areAllMessagesByMe = false;
            }
        }

        if (areAllMessagesByMe) {
            GroupChatActivity.deleteIconCard.setVisibility(View.VISIBLE);
        } else {
            GroupChatActivity.deleteIconCard.setVisibility(View.GONE);
        }
    }

    private void addModelToSelectedList(ForwardModel mm){
        GroupChatActivity.listForSelectedModels.add(mm);

        checkIfCopyToBeVisibleOrNot();
    }

    private void removeModelFromSelectedList(ForwardModel mm) {
        int position = 1110;

        for (int i = 0; i < GroupChatActivity.listForSelectedModels.size(); i++) {
            ForwardModel modelForward = GroupChatActivity.listForSelectedModels.get(i);
            if (modelForward.getMessageKey().equals(mm.getMessageKey())) {
                position = i;
            }
        }

        if (position != 1110) {
            GroupChatActivity.listForSelectedModels.remove(position);
        }

        checkIfCopyToBeVisibleOrNot();
    }

    private boolean containsCheck(ForwardModel mm) {

        boolean contains = false;
        for (ForwardModel modelChat: GroupChatActivity.listForSelectedModels) {
            if (mm.getMessageKey().equals(modelChat.getMessageKey())) {
                contains = true;
            }
        }

        return contains;
    }



    private void deleteMessageClicked(GroupMessageModel model) {
        String[] colors = {"Delete For Me", "Delete For Everyone"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("");

        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {
                    FirebaseDatabase.getInstance().getReference().child("Groups").child(model.getGroupId()).child("Messages").child(model.getKey()).child("deletedBy").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);

                } else if (which == 1) {
                    FirebaseDatabase.getInstance().getReference().child("Groups").child(model.getGroupId()).child("Messages").child(model.getKey()).removeValue();
                }
            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout bgLayout;
        LinearLayout myLayout, otherLayout, headerTypeLayout;
        ImageView otherPersonImageView;

        TextView audioText, audioTextMy, docText, docTextMy, Message_View_Id, Message_View_IdMy, time, timeMy, sender_name;
        ImageView playBtn, playBtnAudio, playBtnMy, playBtnAudioMy, message_image, message_imageMy;
        RelativeLayout audioLayout, audioLayoutMy, docLayout, docLayoutMy, rl_image_messageRow, rl_image_messageRowMy;

        CardView groupLeavingCardView;
        TextView groupLeavingMessage, groupLeavingTime;

        RelativeLayout replyLayoutOther, replyLayoutMy;
        TextView replyMessageSenderNameMy, replyMessageTextMy, replyMessageSenderNameOther, replyMessageTextOther;
        ImageView replyMessageImageMy, replyMessageImageOther;

        AVLoadingIndicatorView avi, aviMy;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            bgLayout = itemView.findViewById(R.id.Message_Layout_Control);

            headerTypeLayout = itemView.findViewById(R.id.headerLayout);
            groupLeavingCardView = itemView.findViewById(R.id.groupLeavingCardView);
            groupLeavingMessage = itemView.findViewById(R.id.groupLeavingMessageGroupMessageRow);
            groupLeavingTime = itemView.findViewById(R.id.groupLeavingTime);

            replyLayoutMy = itemView.findViewById(R.id.replyLayoutMy);
            replyLayoutOther = itemView.findViewById(R.id.replyLayoutOther);

            replyMessageSenderNameMy = itemView.findViewById(R.id.messageSenderNameReplyMessageMy);
            replyMessageSenderNameOther= itemView.findViewById(R.id.messageSenderNameReplyMessageOther);
            replyMessageTextMy = itemView.findViewById(R.id.messageTextReplyMessageMy);
            replyMessageTextOther = itemView.findViewById(R.id.messageTextReplyMessageOther);
            replyMessageImageMy = itemView.findViewById(R.id.messageImageReplyMessageMy);
            replyMessageImageOther = itemView.findViewById(R.id.messageImageReplyMessageOther);

            otherPersonImageView = itemView.findViewById(R.id.otherPersonImageForMessage);

            avi = itemView.findViewById(R.id.avi_message_row);
            aviMy = itemView.findViewById(R.id.avi_message_row_my);

            myLayout = itemView.findViewById(R.id.my_layout);
            otherLayout = itemView.findViewById(R.id.other_layout);

            audioText = itemView.findViewById(R.id.audio_text);
            audioTextMy  = itemView.findViewById(R.id.audio_text_my);

            docText = itemView.findViewById(R.id.doc_text);
            docTextMy = itemView.findViewById(R.id.doc_text_my);

            Message_View_Id = itemView.findViewById(R.id.Message_View_Id);
            Message_View_IdMy = itemView.findViewById(R.id.Message_View_Id_my);

            time = itemView.findViewById(R.id.time_text_message_row);
            timeMy = itemView.findViewById(R.id.time_text_message_row_my);

            sender_name = itemView.findViewById(R.id.sender_name_grp_card);

            playBtn = itemView.findViewById(R.id.play_icon);
            playBtnMy = itemView.findViewById(R.id.play_icon_my);

            playBtnAudioMy = itemView.findViewById(R.id.play_audio_my);
            playBtnAudio = itemView.findViewById(R.id.play_audio);

            message_image = itemView.findViewById(R.id.image_id_message_row);
            message_imageMy = itemView.findViewById(R.id.image_id_message_row_my);

            audioLayout = itemView.findViewById(R.id.audio_layout);
            audioLayoutMy = itemView.findViewById(R.id.audio_layout_my);

            docLayout = itemView.findViewById(R.id.rl_doc_type_layout);
            docLayoutMy = itemView.findViewById(R.id.rl_doc_type_layout_my);
            rl_image_messageRow = itemView.findViewById(R.id.rlchat_message_row);
            rl_image_messageRowMy = itemView.findViewById(R.id.rlchat_message_row_my);
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
    MediaPlayer mp = new MediaPlayer();
}
