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
import android.util.Log;
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
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import codegradients.com.chatapp.Activities.ChatActivity;
import codegradients.com.chatapp.Activities.ImageViewingActivity;
import codegradients.com.chatapp.Activities.VideoPlayingActivity;
import codegradients.com.chatapp.Models.ChatModel;
import codegradients.com.chatapp.Models.ForwardModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.InputStreamVolleyRequest;
import codegradients.com.chatapp.helper_classes.volleyInstance;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    List<ChatModel> list;
    Context context;

    ProgressDialog downloadProgressDialog;

    public ChatAdapter(List<ChatModel> list, Context context) {
        this.list = list;
        this.context = context;

        downloadProgressDialog = new ProgressDialog(context);
        downloadProgressDialog.setTitle("Downloading");
        downloadProgressDialog.setMessage("Downloading For the First time");
        downloadProgressDialog.setCancelable(false);
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {

        final ChatModel model = list.get(position);

        if (model.getMessageType().equals("headerType")) {

            holder.myLayout.setVisibility(View.GONE);
            holder.otherLayout.setVisibility(View.GONE);
            holder.headerLayout.setVisibility(View.VISIBLE);

        } else {
            try {
                if (!model.getSender_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    long timeToBeChecked = Long.parseLong(model.getTimestamp()) + 500;

                    if (timeToBeChecked  > System.currentTimeMillis()) {
                        try {
                            MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.woosh_sound);
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
                    }
                }
            } catch (Exception e) { }

            holder.headerLayout.setVisibility(View.GONE);
            if (model.getSender_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                if (model.getMessageStatus().equals("seen")) {
                    holder.message_status_icon_my.setImageDrawable(context.getResources().getDrawable(R.drawable.seen_icon));
                } else if (model.getMessageStatus().equals("delivered")) {
                    holder.message_status_icon_my.setImageDrawable(context.getResources().getDrawable(R.drawable.delivered_icon));
                } else {
                    holder.message_status_icon_my.setImageDrawable(context.getResources().getDrawable(R.drawable.sent_icon));
                }
            } else {

                if (!model.getMessageStatus().equals("seen")) {
                    DatabaseReference mDatabaseForUpdating = FirebaseDatabase.getInstance().getReference().child("Chats").child(model.getSender_id()).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(model.getMessageKey());
                    mDatabaseForUpdating.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                if (dataSnapshot.hasChild("message")) {

                                    if (dataSnapshot.hasChild("messageStatus")) {
                                        if (!dataSnapshot.child("messageStatus").getValue(String.class).equals("seen")) {
                                            mDatabaseForUpdating.child("messageStatus").setValue("seen");
                                            mDatabaseForUpdating.child("readTime").setValue(String.valueOf(System.currentTimeMillis()));
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            if (model.getSender_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                //my message
                holder.myLayout.setVisibility(View.VISIBLE);
                holder.otherLayout.setVisibility(View.GONE);

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

                holder.replyLayoutMy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "To Be Implemented", Toast.LENGTH_SHORT).show();
                    }
                });

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(model.getTimestamp()));

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

                String messageTime = "";

                if (minute < 10) {
                    messageTime = String.valueOf(hour) + ":0" + String.valueOf(calendar.get(Calendar.MINUTE)) + " " + format;
                    //holder.timeMy.setText(messageTime);
                } else {
                    messageTime = String.valueOf(hour) + ":" + String.valueOf(calendar.get(Calendar.MINUTE)) + " " + format;
                    //holder.timeMy.setText(messageTime);
                }

                Calendar today = Calendar.getInstance();
                today.setTimeInMillis(System.currentTimeMillis());

                if (calendar.get(Calendar.DATE) == today.get(Calendar.DATE)) {
                    messageTime = messageTime;
                    holder.timeMy.setText(messageTime);
                } else if (calendar.get(Calendar.DATE) == (today.get(Calendar.DATE) - 1)) {
                    messageTime = messageTime + ", Yesterday";
                    holder.timeMy.setText(messageTime);
                } else {

                    int monn = calendar.get(Calendar.MONTH);
                    monn++;

                    messageTime = messageTime + ", " + calendar.get(Calendar.DATE) + "/" + monn + "/" + calendar.get(Calendar.YEAR);
                    holder.timeMy.setText(messageTime);
                }

                holder.aviMy.setVisibility(View.GONE);
                holder.rl_image_messageRowMy.setVisibility(View.GONE);
                holder.Message_View_IdMy.setVisibility(View.GONE);
                holder.docLayoutMy.setVisibility(View.GONE);

                if (model.getMessageType().equals("img") || model.getMessageType().equals("video")) {

                    if (model.getMessageType().equals("video")) {
                        holder.playBtnMy.setVisibility(View.VISIBLE);
                    } else {
                        holder.playBtnMy.setVisibility(View.GONE);
                    }

                    holder.playBtnMy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            context.startActivity(new Intent(context, VideoPlayingActivity.class).putExtra("videoURL", model.getMessage()).putExtra("headingText", model.getSender_name()));
                        }
                    });

                    holder.audioLayoutMy.setVisibility(View.GONE);
                    holder.rl_image_messageRowMy.setVisibility(View.VISIBLE);
                    holder.aviMy.setVisibility(View.VISIBLE);
                    holder.docLayoutMy.setVisibility(View.GONE);
                    holder.messageImageMy.setVisibility(View.VISIBLE);
                    holder.Message_View_IdMy.setVisibility(View.GONE);
                    Glide.with(context).load(model.getMessage()).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.aviMy.setVisibility(View.GONE);
                            holder.messageImageMy.setImageResource(R.drawable.ic_camera);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.aviMy.setVisibility(View.GONE);
                            return false;
                        }
                    }).into(holder.messageImageMy);

                } else if (model.getMessageType().equals("text")) {
                    holder.messageImageMy.setVisibility(View.GONE);
                    holder.docLayoutMy.setVisibility(View.GONE);
                    holder.audioLayoutMy.setVisibility(View.GONE);
                    holder.Message_View_IdMy.setVisibility(View.VISIBLE);
                    holder.Message_View_IdMy.setText(model.getMessage());

                } else if (model.getMessageType().equals("doc") || model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt") || model.getMessageType().equals("pdf") || model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls") || model.getMessageType().equals("docx")) {
                    holder.docTextMy.setText(model.getMessageKey() + "." + model.getMessageType());
                    holder.rl_image_messageRowMy.setVisibility(View.GONE);
                    holder.messageImageMy.setVisibility(View.GONE);
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

                            File file = new File(root, model.getMessageKey() + "." + model.getMessageType());
                            if (file.exists()) {
                                //loadPdf(file);

                                Uri path = Uri.fromFile(file);
                                Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                                pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                if (model.getMessageType().equals("doc") || model.getMessageType().equals("docx")) {
                                    pdfOpenintent.setDataAndType(path, "application/msword");
                                } else if (model.getMessageType().equals("pdf")) {
                                    pdfOpenintent.setDataAndType(path, "application/pdf");
                                } else if (model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt")) {
                                    pdfOpenintent.setDataAndType(path, "application/vnd.ms-powerpoint");
                                } else if (model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls")) {
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
                                                String name = model.getMessageKey() + "." + model.getMessageType();
                                                outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
                                                outputStream.write(response);
                                                outputStream.close();

                                                File Originalfile = new File(context.getFilesDir().getPath() + "/" + model.getMessageKey() + "." + model.getMessageType());

                                                File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Document");
                                                if (!root.exists())
                                                    root.mkdirs();
                                                File file = new File(root, model.getMessageKey() + "." + model.getMessageType());

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
                                                    if (model.getMessageType().equals("doc") || model.getMessageType().equals("docx")) {
                                                        pdfOpenintent.setDataAndType(path, "application/msword");
                                                    } else if (model.getMessageType().equals("pdf")) {
                                                        pdfOpenintent.setDataAndType(path, "application/pdf");
                                                    } else if (model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt")) {
                                                        pdfOpenintent.setDataAndType(path, "application/vnd.ms-powerpoint");
                                                    } else if (model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls")) {
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

                    holder.audioTextMy.setText(model.getMessageKey());

                    holder.rl_image_messageRowMy.setVisibility(View.GONE);
                    holder.messageImageMy.setVisibility(View.GONE);
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

                            File file = new File(root, model.getMessageKey() + ".mp3");
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
                                                String name = model.getMessageKey() + ".mp3";
                                                outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
                                                outputStream.write(response);
                                                outputStream.close();

                                                File Originalfile = new File(context.getFilesDir().getPath() + "/" + model.getMessageKey() + ".mp3");

                                                File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Audio");
                                                if (!root.exists())
                                                    root.mkdirs();
                                                File file = new File(root, model.getMessageKey() + ".mp3");

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

            } else {
                //Message From Other Person
                holder.myLayout.setVisibility(View.GONE);
                holder.otherLayout.setVisibility(View.VISIBLE);

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

                holder.replyLayoutOther.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "To Be Implemented", Toast.LENGTH_SHORT).show();
                    }
                });

                if (model.getMessageType().equals("text")) {
                    holder.Message_View_Id.setText(model.getMessage());
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(model.getTimestamp()));

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

                String messageTime = "";


                if (minute < 10) {
                    messageTime = String.valueOf(hour) + ":0" + String.valueOf(calendar.get(Calendar.MINUTE)) + " " + format;
                    //holder.time.setText(messageTime);
                } else {
                    messageTime = String.valueOf(hour) + ":" + String.valueOf(calendar.get(Calendar.MINUTE)) + " " + format;
                    //holder.time.setText(messageTime);
                }

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
                    mon++;

                    messageTime = messageTime + ", " + calendar.get(Calendar.DATE) + "/" + mon + "/" + calendar.get(Calendar.YEAR);
                    holder.time.setText(messageTime);
                }

                holder.avi.setVisibility(View.GONE);

                holder.rl_image_messageRow.setVisibility(View.GONE);

                if (model.getMessageType().equals("img") || model.getMessageType().equals("video")) {

                    if (model.getMessageType().equals("video")) {
                        holder.playBtn.setVisibility(View.VISIBLE);
                    } else {
                        holder.playBtn.setVisibility(View.GONE);
                    }

                    holder.playBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            context.startActivity(new Intent(context, VideoPlayingActivity.class).putExtra("videoURL", model.getMessage()).putExtra("headingText", model.getSender_name()));
                        }
                    });

                    holder.audioLayout.setVisibility(View.GONE);
                    holder.rl_image_messageRow.setVisibility(View.VISIBLE);
                    holder.avi.setVisibility(View.VISIBLE);
                    holder.messageImage.setVisibility(View.VISIBLE);
                    holder.Message_View_Id.setVisibility(View.GONE);
                    holder.docLayout.setVisibility(View.GONE);

                    Glide.with(context).load(model.getMessage()).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.avi.setVisibility(View.GONE);
                            holder.messageImage.setImageResource(R.drawable.ic_camera);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.avi.setVisibility(View.GONE);
                            return false;
                        }
                    }).into(holder.messageImage);

                } else if (model.getMessageType().equals("text")) {
                    holder.messageImage.setVisibility(View.GONE);
                    holder.Message_View_Id.setVisibility(View.VISIBLE);
                    holder.audioLayout.setVisibility(View.GONE);

                    holder.Message_View_Id.setText(model.getMessage());

                    //holder.Message_View_Id.setText(model.getMessage());
                    holder.docLayout.setVisibility(View.GONE);
                } else if (model.getMessageType().equals("doc") || model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt") || model.getMessageType().equals("pdf") || model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls") || model.getMessageType().equals("docx")) {
                    holder.docText.setText(model.getMessageKey() + "." + model.getMessageType());
                    holder.messageImage.setVisibility(View.GONE);
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

                            File file = new File(root, model.getMessageKey() + "." + model.getMessageType());
                            if (file.exists()) {
                                //loadPdf(file);

                                Uri path = Uri.fromFile(file);
                                Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                                pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                if (model.getMessageType().equals("doc") || model.getMessageType().equals("docx")) {
                                    pdfOpenintent.setDataAndType(path, "application/msword");
                                } else if (model.getMessageType().equals("pdf")) {
                                    pdfOpenintent.setDataAndType(path, "application/pdf");
                                } else if (model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt")) {
                                    pdfOpenintent.setDataAndType(path, "application/vnd.ms-powerpoint");
                                } else if (model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls")) {
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
                                                String name = model.getMessageKey() + "." + model.getMessageType();
                                                outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
                                                outputStream.write(response);
                                                outputStream.close();

                                                File Originalfile = new File(context.getFilesDir().getPath() + "/" + model.getMessageKey() + "." + model.getMessageType());

                                                File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Document");
                                                if (!root.exists())
                                                    root.mkdirs();
                                                File file = new File(root, model.getMessageKey() + "." + model.getMessageType());

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
                                                    if (model.getMessageType().equals("doc") || model.getMessageType().equals("docx")) {
                                                        pdfOpenintent.setDataAndType(path, "application/msword");
                                                    } else if (model.getMessageType().equals("pdf")) {
                                                        pdfOpenintent.setDataAndType(path, "application/pdf");
                                                    } else if (model.getMessageType().equals("pptx") || model.getMessageType().equals("ppt")) {
                                                        pdfOpenintent.setDataAndType(path, "application/vnd.ms-powerpoint");
                                                    } else if (model.getMessageType().equals("xlsx") || model.getMessageType().equals("xls")) {
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
                    holder.audioText.setText(model.getMessageKey());
                    holder.messageImage.setVisibility(View.GONE);
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

                            File file = new File(root, model.getMessageKey() + ".mp3");
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
                                                String name = model.getMessageKey() + ".mp3";
                                                outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
                                                outputStream.write(response);
                                                outputStream.close();

                                                File Originalfile = new File(context.getFilesDir().getPath() + "/" + model.getMessageKey() + ".mp3");

                                                File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Audio");
                                                if (!root.exists())
                                                    root.mkdirs();
                                                File file = new File(root, model.getMessageKey() + ".mp3");

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
            }

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    Log.v("clickingCheck__", "LongClickPosition: " + holder.getAdapterPosition() + "   type: " + model.getMessageType());

                    if (holder.getAdapterPosition() == 0) {
                        return true;
                    }

                    if (ChatActivity.isSelectionModeOn) {
                        ForwardModel mm = new ForwardModel(model.getMessageType(), model.getMessageKey(), model.getMessage(), model.getSender_id());

                        if (containsCheck(mm)) {
                            //Toast.makeText(context, "Contains", Toast.LENGTH_SHORT).show();
                            removeModelFromSelectedList(mm);
                            holder.bgLayout.setBackgroundColor(Color.parseColor("#ffffff"));
                        } else {
                            //Toast.makeText(context, " Do Not Contains", Toast.LENGTH_SHORT).show();
                            addModelToSelectedList(mm);
                            holder.bgLayout.setBackgroundColor(Color.parseColor("#333B61F6"));
                        }
                    } else {
                        ChatActivity.isSelectionModeOn = true;
                        ChatActivity.selectionOptionsLayoutCard.setVisibility(View.VISIBLE);

                        ForwardModel mm = new ForwardModel(model.getMessageType(), model.getMessageKey(), model.getMessage(), model.getSender_id());

                        if (containsCheck(mm)) {
                            //Toast.makeText(context, "Contains", Toast.LENGTH_SHORT).show();
                            removeModelFromSelectedList(mm);
                            holder.bgLayout.setBackgroundColor(Color.parseColor("#ffffff"));
                        } else {
                            //Toast.makeText(context, " Do Not Contains", Toast.LENGTH_SHORT).show();
                            addModelToSelectedList(mm);
                            holder.bgLayout.setBackgroundColor(Color.parseColor("#333B61F6"));
                        }
                    }

                    return true;
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.v("clickingCheck__", "NormalClickPosition: " + position + "   type: " + model.getMessageType());

                    if (holder.getAdapterPosition() == 0) {
                        return;
                    }

                    if (!ChatActivity.isSelectionModeOn) {
                        if (model.getMessageType().equals("img")) {
                            context.startActivity(new Intent(context, ImageViewingActivity.class).putExtra("URL", model.getMessage()));
                        }
                    } else {
                        ForwardModel mm = new ForwardModel(model.getMessageType(), model.getMessageKey(), model.getMessage(), model.getSender_id());

                        if (containsCheck(mm)) {
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

            try {
                ForwardModel mm = new ForwardModel(model.getMessageType(), model.getMessageKey(), model.getMessage(), model.getSender_id());

                if (containsCheck(mm)) {
                    holder.bgLayout.setBackgroundColor(Color.parseColor("#333B61F6"));
                } else {
                    holder.bgLayout.setBackgroundColor(Color.parseColor("#ffffff"));
                }
            } catch (Exception e) {

            }
        }
    }

    private void checkIfCopyToBeVisibleOrNot() {
        boolean oneMessageIsNotText = false;

        for (ForwardModel modelForward : ChatActivity.listForSelectedModels) {
            if (!modelForward.getMessageType().equals("text")) {
                oneMessageIsNotText = true;
            }
        }

        if (oneMessageIsNotText) {
            ChatActivity.copyIconCard.setVisibility(View.GONE);
        } else {
            ChatActivity.copyIconCard.setVisibility(View.VISIBLE);
        }

        boolean areAllMessagesByMe = true;

        for (ForwardModel modelForward : ChatActivity.listForSelectedModels) {
            if (!modelForward.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                areAllMessagesByMe = false;
            }
        }

        if (areAllMessagesByMe) {
            ChatActivity.deleteIconCard.setVisibility(View.VISIBLE);
        } else {
            ChatActivity.deleteIconCard.setVisibility(View.GONE);
        }

        if (ChatActivity.listForSelectedModels.size() > 1) {
            ChatActivity.replyIcon.setVisibility(View.GONE);
        } else {
            ChatActivity.replyIcon.setVisibility(View.VISIBLE);
        }
    }

    private void addModelToSelectedList(ForwardModel mm) {
        ChatActivity.listForSelectedModels.add(mm);

        checkIfCopyToBeVisibleOrNot();
    }

    private void removeModelFromSelectedList(ForwardModel mm) {
        int position = 1110;

        for (int i = 0; i < ChatActivity.listForSelectedModels.size(); i++) {
            ForwardModel modelForward = ChatActivity.listForSelectedModels.get(i);
            if (modelForward.getMessageKey().equals(mm.getMessageKey())) {
                position = i;
            }
        }

        if (position != 1110) {
            ChatActivity.listForSelectedModels.remove(position);
        }

        checkIfCopyToBeVisibleOrNot();
    }

    private boolean containsCheck(ForwardModel mm) {

        boolean contains = false;
        for (ForwardModel modelChat : ChatActivity.listForSelectedModels) {
            if (mm.getMessageKey().equals(modelChat.getMessageKey())) {
                contains = true;
            }
        }

        return contains;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout otherLayout, myLayout, headerLayout;

        ImageView playBtn, playBtnMy, playBtnAudio, playBtnAudioMy;

        ImageView messageImage, messageImageMy, message_status_icon_my;

        TextView audioText, audioTextMy, time, timeMy, Message_View_Id, Message_View_IdMy, docText, docTextMy;

        RelativeLayout rl_image_messageRow, rl_image_messageRowMy, docLayout, docLayoutMy, audioLayout, audioLayoutMy;
        AVLoadingIndicatorView avi, aviMy;

        RelativeLayout bgLayout;

        RelativeLayout replyLayoutOther, replyLayoutMy;
        TextView replyMessageSenderNameMy, replyMessageTextMy, replyMessageSenderNameOther, replyMessageTextOther;
        ImageView replyMessageImageMy, replyMessageImageOther;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bgLayout = itemView.findViewById(R.id.Message_Layout_Control);

            headerLayout = itemView.findViewById(R.id.headerLayout);
            otherLayout = itemView.findViewById(R.id.other_layout);
            myLayout = itemView.findViewById(R.id.my_layout);

            replyLayoutMy = itemView.findViewById(R.id.replyLayoutMy);
            replyLayoutOther = itemView.findViewById(R.id.replyLayoutOther);

            replyMessageSenderNameMy = itemView.findViewById(R.id.messageSenderNameReplyMessageMy);
            replyMessageSenderNameOther= itemView.findViewById(R.id.messageSenderNameReplyMessageOther);
            replyMessageTextMy = itemView.findViewById(R.id.messageTextReplyMessageMy);
            replyMessageTextOther = itemView.findViewById(R.id.messageTextReplyMessageOther);
            replyMessageImageMy = itemView.findViewById(R.id.messageImageReplyMessageMy);
            replyMessageImageOther = itemView.findViewById(R.id.messageImageReplyMessageOther);

            playBtn = itemView.findViewById(R.id.play_icon);
            playBtnMy = itemView.findViewById(R.id.play_icon_my);

            message_status_icon_my = itemView.findViewById(R.id.message_status_icon_my);

            playBtnAudio = itemView.findViewById(R.id.play_audio);
            playBtnAudioMy = itemView.findViewById(R.id.play_audio_my);

            messageImage = itemView.findViewById(R.id.image_id_message_row);
            messageImageMy = itemView.findViewById(R.id.image_id_message_row_my);

            audioText = itemView.findViewById(R.id.audio_text);
            audioTextMy = itemView.findViewById(R.id.audio_text_my);

            time = itemView.findViewById(R.id.time_text_message_row);
            timeMy = itemView.findViewById(R.id.time_text_message_row_my);

            Message_View_Id = itemView.findViewById(R.id.Message_View_Id);
            Message_View_IdMy = itemView.findViewById(R.id.Message_View_Id_my);

            docText = itemView.findViewById(R.id.doc_text_message_row);
            docTextMy = itemView.findViewById(R.id.doc_text_message_row_my);

            rl_image_messageRow = itemView.findViewById(R.id.rlchat_message_row);
            rl_image_messageRowMy = itemView.findViewById(R.id.rlchat_message_row_my);

            docLayout = itemView.findViewById(R.id.rl_doc_type_layout);
            docLayoutMy = itemView.findViewById(R.id.rl_doc_type_layout_my);

            audioLayout = itemView.findViewById(R.id.audio_layout);
            audioLayoutMy = itemView.findViewById(R.id.audio_layout_my);

            avi = itemView.findViewById(R.id.avi_message_row);
            aviMy = itemView.findViewById(R.id.avi_message_row_my);
        }

    }

    private void deleteMessageClicked(ChatModel model) {
        String[] colors = {"Delete For Me", "Delete For Everyone"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("");

        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == 0) {

                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(ChatActivity.receiverId);
                    mDatabase.child(model.getMessageKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Message Deleted For You", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else if (which == 1) {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(ChatActivity.receiverId);
                    mDatabase.child(model.getMessageKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Chats").child(ChatActivity.receiverId).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                mDatabase2.child(model.getMessageKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task2) {
                                        if (task2.isSuccessful()) {
                                            Toast.makeText(context, "Message Deleted For Everyone", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "Error Deleting On Other Side: " + task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(context, "Error Deleting On Your Side: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        builder.show();
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
