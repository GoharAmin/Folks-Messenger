package codegradients.com.chatapp.adapters;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import codegradients.com.chatapp.Activities.ImageViewingActivity;
import codegradients.com.chatapp.Activities.VideoPlayingActivity;
import codegradients.com.chatapp.Models.ScheduleMessageModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.InputStreamVolleyRequest;
import codegradients.com.chatapp.helper_classes.volleyInstance;

public class ScheduledMessageAdapter extends RecyclerView.Adapter<ScheduledMessageAdapter.ViewHolder> {

    List<ScheduleMessageModel> list;
    Context context;

    ProgressDialog downloadProgressDialog;

    public ScheduledMessageAdapter(List<ScheduleMessageModel> list, Context context) {
        this.list = list;
        this.context = context;

        downloadProgressDialog = new ProgressDialog(context);
        downloadProgressDialog.setTitle("Downloading");
        downloadProgressDialog.setMessage("Downloading For the First time");
        downloadProgressDialog.setCancelable(false);
    }

    @NonNull
    @Override
    public ScheduledMessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.scheduled_message_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduledMessageAdapter.ViewHolder holder, int position) {

        ScheduleMessageModel model = list.get(position);

        holder.receipientName.setText(model.getReceiver_name());

        if (model.getReceiverType() == 1){
            holder.receipientName.setText(model.getReceiver_name() + "(Group)");
        }

        holder.myLayout.setVisibility(View.VISIBLE);

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

//            holder.Message_View_IdMy.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//
//                    String[] colors = {"Copy"};
//
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    builder.setTitle("Copy Message");
//                    builder.setItems(colors, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // the user clicked on colors[which]
//                            if (which == 0) {
//                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//                                ClipData clip = ClipData.newPlainText("message", holder.Message_View_IdMy.getText().toString());
//                                clipboard.setPrimaryClip(clip);
//                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                    builder.show();
//                    return true;
//                }
//            });

//            holder.messageImageMy.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//
//                    String[] colors = {"Copy Image"};
//
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    builder.setTitle("");
//                    builder.setItems(colors, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            // the user clicked on colors[which]
//                            if (which == 0) {
//                                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//                                ClipData clip = ClipData.newPlainText("message", model.getMessage());
//                                clipboard.setPrimaryClip(clip);
//                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                    builder.show();
//                    return true;
//                }
//            });

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

                    context.startActivity(new Intent(context, VideoPlayingActivity.class).putExtra("videoURL", model.getMessage()).putExtra("headingText", model.getReceiver_name()));

//                        //downloading and playing audio
//
//// image naming and path  to include sd card  appending name you choose for file
//                        //final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Edo4All/Videos";
//                        File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Videos");
//                        if (!root.exists())
//                            root.mkdirs();
//
//                        File file = new File(root, model.getMessageKey() + ".mp4");
//                        if (file.exists()) {
//                            //loadPdf(file);
//
//                            Uri fileUri = Uri.fromFile(file);
//
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
//                                            String name = model.getMessageKey() + ".mp4";
//                                            outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
//                                            outputStream.write(response);
//                                            outputStream.close();
//
//                                            File Originalfile = new File(context.getFilesDir().getPath() + "/" + model.getMessageKey() + ".mp4");
//
//                                            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Edo4All/Videos");
//                                            if (!root.exists())
//                                                root.mkdirs();
//                                            File file = new File(root, model.getMessageKey() + ".mp4");
//
//                                            try {
//                                                FileUtils.copyFile(Originalfile, file);
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//
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

            if (model.getMessageType().equals("img")){
                holder.messageImageMy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(new Intent(context, ImageViewingActivity.class).putExtra("URL", model.getMessage()));
                    }
                });
            }

        } else if (model.getMessageType().equals("text")) {
            holder.messageImageMy.setVisibility(View.GONE);
            holder.docLayoutMy.setVisibility(View.GONE);
            holder.audioLayoutMy.setVisibility(View.GONE);
            holder.Message_View_IdMy.setVisibility(View.VISIBLE);

            holder.Message_View_IdMy.setText(model.getMessage());


            //holder.Message_View_IdMy.setText(model.getMessage());


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

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView receipientName;

        LinearLayout myLayout;

        ImageView playBtnMy, playBtnAudioMy;

        ImageView  messageImageMy, message_status_icon_my;

        TextView  audioTextMy, timeMy,  Message_View_IdMy,  docTextMy;

        RelativeLayout  rl_image_messageRowMy, docLayoutMy, audioLayoutMy;
        AVLoadingIndicatorView  aviMy;

        RelativeLayout bgLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            receipientName = itemView.findViewById(R.id.recipientNameScheduledMessageCard);

            bgLayout = itemView.findViewById(R.id.Message_Layout_Control);

            myLayout = itemView.findViewById(R.id.my_layout);

            playBtnMy = itemView.findViewById(R.id.play_icon_my);

            message_status_icon_my = itemView.findViewById(R.id.message_status_icon_my);

            playBtnAudioMy = itemView.findViewById(R.id.play_audio_my);
            messageImageMy = itemView.findViewById(R.id.image_id_message_row_my);

            audioTextMy = itemView.findViewById(R.id.audio_text_my);

            timeMy = itemView.findViewById(R.id.time_text_message_row_my);

            Message_View_IdMy = itemView.findViewById(R.id.Message_View_Id_my);

            docTextMy = itemView.findViewById(R.id.doc_text_message_row_my);

            rl_image_messageRowMy = itemView.findViewById(R.id.rlchat_message_row_my);

            docLayoutMy = itemView.findViewById(R.id.rl_doc_type_layout_my);

            audioLayoutMy = itemView.findViewById(R.id.audio_layout_my);

            aviMy = itemView.findViewById(R.id.avi_message_row_my);
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
