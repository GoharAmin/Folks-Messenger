package codegradients.com.chatapp.helper_classes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import codegradients.com.chatapp.BuildConfig;
import codegradients.com.chatapp.Models.ChatModel;
import codegradients.com.chatapp.Models.ForwardModel;
import codegradients.com.chatapp.Models.GroupMessageModel;
import codegradients.com.chatapp.R;

public class HelperClass {

    Context context;

    public static String chattingWithId = "";

    public static List<ForwardModel> forwardModelListToForwardMessages = new ArrayList<>();

    public HelperClass(Context context) {
        this.context = context;

        showWaitingDialog();
    }

    public AlertDialog progressDialogAlert;
    public TextView textInProgressDialog;
    public CircularProgressBar progressBarProgressDialog;

    public void showWaitingDialog(){

        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        //setting up the layout for alert dialog
        View view1 = LayoutInflater.from(context).inflate(R.layout.dialog_progress_layout, null, false);

        builder1.setView(view1);

        textInProgressDialog = view1.findViewById(R.id.text_progress_layout);
        progressBarProgressDialog = view1.findViewById(R.id.circularProgressBarCircularProgressDialog);
        progressBarProgressDialog.setIndeterminateMode(true);
        progressDialogAlert = builder1.create();
        progressDialogAlert.setCancelable(false);
        progressDialogAlert.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    public static void createToast(Context context, String message) {
        Toast.makeText(context, ""+message, Toast.LENGTH_SHORT).show();
    }


    public static Uri saveImageToInternalStorage(Context context, Bitmap bitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/holst messanger");
        myDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "img_" + timeStamp + ".jpg";

        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("exce","Output strem Exception"+e.getMessage());
        }
        return Uri.fromFile(file);
    }
    public  static String getFileNameFromUri(Uri uri){
        String path = uri.getPath();
        String filename = path.substring(path.lastIndexOf("/")+1);
        String file;
        if (filename.indexOf(".") > 0) {
            return filename.substring(0, filename.lastIndexOf("."));
        } else {
            return file =  filename;
        }
    }
    public static long getDurationOfVideo(Uri uri,Context context) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(context, uri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time );

        retriever.release();
        return timeInMillisec;
    }

    public static long getNoOfHours(String time) {
        long diffInMillisec = System.currentTimeMillis() - Long.parseLong(time);
        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMillisec);
        long  seconds = diffInSec % 60;
        Log.i("asdfas", ""+seconds);
        diffInSec/= 60;
        long  minutes =diffInSec % 60;
        Log.i("asdfas", ""+minutes);
        diffInSec /= 60;
        long hours = diffInSec % 24;
        Log.i("asdfas", ""+hours);
        return hours;
    }
    public static String formateTime(long time) {

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        Date resultdate = new Date(time);

        Log.i("afvavasf",""+sdf.format(resultdate));
        return sdf.format(resultdate);
    }

    public static String formateDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date resultdate = new Date(time);
        return sdf.format(resultdate);

    }

    public static void deleteMessageWithTimer(ChatModel model, Activity activity, String receiverId) {

        Thread timer = new Thread(){
            public void run() {
                try {
                    sleep(Long.parseLong(model.getAutoDestructionTime()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                deleteTheMessage(model.getMessageKey());

                                DatabaseReference mDatabaseForDeletingMessageFromHis = FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverId).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(model.getMessageKey());
                                DatabaseReference mDatabaseForDeletingMessageFromMy =  FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(receiverId).child(model.getMessageKey());

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
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timer.start();
    }

    public static void sendNotificationForPersonelChat(final Context context, String token, ChatModel model, String message, String receiverImage){
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            JSONObject object = new JSONObject();
            JSONArray regIds = new JSONArray();
            regIds.put(token);
            object.put("registration_ids", regIds);
            JSONObject obj = new JSONObject();

            obj.put("message", message);
            obj.put("userId", model.getSender_id());

            try {
                obj.put("userName", decrypt(model.getSender_name()));
            } catch (Exception e){
                obj.put("userName", model.getSender_name());
            }


            obj.put("userImage", receiverImage);
            obj.put("timeForMessage", Long.parseLong(model.getTimestamp()));
            obj.put("type", "scheduledMessageSingle");

//            obj.put("title", title);
//            obj.put("body", body);
            object.put("data", obj);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", context.getResources().getString(R.string.firebase_message_key));
                    return headers;
                }
            };
            volleyInstance.getmInstance(context).addToRequestQueue(request);
        } catch (Exception e) { }
    }

    public static void sendNotificationForGroupChat(final Context context, String token, GroupMessageModel model, String message, String groupName){
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            JSONObject object = new JSONObject();
            JSONArray regIds = new JSONArray();
            regIds.put(token);
            object.put("registration_ids", regIds);
            JSONObject obj = new JSONObject();

            obj.put("message", message);
            obj.put("userId", model.getFrom());
            obj.put("userName", model.getSenderName());
            obj.put("grpId", model.getGroupId());
            obj.put("grpName", groupName);
            obj.put("timeForMessage", Long.parseLong(model.getTimestamp()));
            obj.put("type", "scheduledMessageGroup");

//            obj.put("title", title);
//            obj.put("body", body);
            object.put("data", obj);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", context.getResources().getString(R.string.firebase_message_key));
                    return headers;
                }
            };
            volleyInstance.getmInstance(context).addToRequestQueue(request);
        } catch (Exception e) { }
    }

    public static void sendFriendRequestNotification(final Context context, HashMap Data, String token) {
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            JSONObject object = new JSONObject();
            JSONArray regIds = new JSONArray();
            regIds.put(token);
            object.put("registration_ids", regIds);
            JSONObject obj = new JSONObject();

            obj.put("message", "friendRequest");
            obj.put("personId", Data.get("senderId"));
            obj.put("personName", Data.get("senderName"));
            obj.put("personImage", Data.get("senderImage"));
            obj.put("time", Data.get("time"));
            obj.put("type", "messageRequest");
//            obj.put("title", title);
//            obj.put("body", body);
            object.put("data", obj);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", context.getResources().getString(R.string.firebase_message_key));
                    return headers;
                }
            };
            volleyInstance.getmInstance(context).addToRequestQueue(request);
        } catch (Exception e) { }
    }

    public static void sendNotificationOfGroupLeaving(final Context context, String groupId, String groupName, String lefterName, String token) {
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            JSONObject object = new JSONObject();
            JSONArray regIds = new JSONArray();
            regIds.put(token);
            object.put("registration_ids", regIds);
            JSONObject obj = new JSONObject();

            obj.put("lefterName", lefterName);
            obj.put("groupName", groupName);
            obj.put("groupId", groupId);
            obj.put("type", "groupLeaving");
//            obj.put("title", title);
//            obj.put("body", body);
            object.put("data", obj);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", context.getResources().getString(R.string.firebase_message_key));
                    return headers;
                }
            };
            volleyInstance.getmInstance(context).addToRequestQueue(request);
        } catch (Exception e) { }
    }

    public static void sendNotificationOfGroupAdding(final Context context, String groupId, String groupName, String addedName, String token, String adderName) {
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            JSONObject object = new JSONObject();
            JSONArray regIds = new JSONArray();
            regIds.put(token);
            object.put("registration_ids", regIds);
            JSONObject obj = new JSONObject();

            obj.put("addedName", addedName);
            obj.put("groupName", groupName);
            obj.put("groupId", groupId);
            obj.put("adderName", adderName);
            obj.put("type", "groupJoining");
//            obj.put("title", title);
//            obj.put("body", body);
            object.put("data", obj);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", context.getResources().getString(R.string.firebase_message_key));
                    return headers;
                }
            };
            volleyInstance.getmInstance(context).addToRequestQueue(request);
        } catch (Exception e) { }
    }

    public static void sendNotificationOfChatRequestStatus(final Context context, HashMap Data, String token, int requestStatus) {
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            JSONObject object = new JSONObject();
            JSONArray regIds = new JSONArray();
            regIds.put(token);
            object.put("registration_ids", regIds);
            JSONObject obj = new JSONObject();


            if (requestStatus == 0) {
                //Accepted
                obj.put("message", "acceptedRequest");
            } else if (requestStatus == 1) {
                obj.put("message", "declinedRequest");
            }
            obj.put("personId", Data.get("receiverId"));
            obj.put("personImage", Data.get("receiverImage"));
            obj.put("personName", Data.get("receiverName"));
            obj.put("time", Data.get("time"));
            obj.put("type", "messageRequest");

            Log.v("FriendReq__", "Here Name: " + Data.get("receiverName"));

            //obj.put("type", "scheduledMessageSingle");
//            obj.put("title", title);
//            obj.put("body", body);
            object.put("data", obj);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", context.getResources().getString(R.string.firebase_message_key));
                    return headers;
                }
            };
            volleyInstance.getmInstance(context).addToRequestQueue(request);
        } catch (Exception e) { }
    }

    public static void sendNotificationForCall(Context context, String token, String callId, String myName) {
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            JSONObject object = new JSONObject();
            JSONArray regIds = new JSONArray();
            regIds.put(token);
            object.put("registration_ids", regIds);
            JSONObject obj = new JSONObject();

            obj.put("callId", callId);
            obj.put("callerName", myName);
            obj.put("type", "callType");
//            obj.put("title", title);
//            obj.put("body", body);
            object.put("data", obj);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", context.getResources().getString(R.string.firebase_message_key));
                    return headers;
                }
            };
            volleyInstance.getmInstance(context).addToRequestQueue(request);
        } catch (Exception e) { }
    }

    static String typeee = "";
    public static void Send_NotificationForGroupMessage(String groupId, String groupName, String myName, GroupMessageModel messageModel) {

        typeee = "";

        if (messageModel.getMessageType().equals("text")) {
            try {
                typeee = messageModel.getMessage();
            } catch (Exception e) {
                typeee = "Text Message";
            }
        } else if (messageModel.getMessageType().equals("audio")) {
            typeee = "Audio";
        } else if (messageModel.getMessageType().equals("img")) {
            typeee = "Image";
        } else if (messageModel.getMessageType().equals("video")) {
            typeee = "Video";
        }

        FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userKeySnapShot: dataSnapshot.getChildren()) {
                    if (!userKeySnapShot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        try {
                            DatabaseReference Notification_Reference = FirebaseDatabase.getInstance().getReference("GroupNotifications").child(groupId).child(groupName).child(userKeySnapShot.getKey()).child(myName).child(typeee);
                            String Notif_Id = Notification_Reference.push().getKey();
                            HashMap Notif_map = new HashMap();
                            Notif_map.put("To", userKeySnapShot.getKey());
                            Notif_map.put("From", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            Notif_map.put("Type", "GroupMessage");

                            Log.v("GroupNotngSent__", "Data: " + Notif_map);

                            Notification_Reference.child(Notif_Id).updateChildren(Notif_map);
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

    public static void Send_NotificationForSingleMessage(String messageType, ChatModel chatModel, String myName) {
        DatabaseReference Notification_Reference;

        String type = "";

        if (messageType.equals("text")) {
            type = chatModel.getMessage();
        } else if (messageType.equals("audio")) {
            type = "Audio";
        } else if (messageType.equals("img")) {
            type = "Image";
        } else if (messageType.equals("video")) {
            type = "Video";
        }

        try {
            Notification_Reference = FirebaseDatabase.getInstance().getReference("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(myName).child(chatModel.getReceiver_id()).child(chatModel.getReceiver_name()).child(type);
        } catch (Exception e) {
            e.printStackTrace();
            Notification_Reference = FirebaseDatabase.getInstance().getReference("Notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(myName).child(chatModel.getReceiver_id()).child(chatModel.getReceiver_name()).child("TextMessage");
        }

        String Notif_Id = Notification_Reference.push().getKey();
        HashMap Notif_map = new HashMap();
        Notif_map.put("To", chatModel.getReceiver_id());
        Notif_map.put("From", FirebaseAuth.getInstance().getCurrentUser().getUid());
        Notif_map.put("Type", "Message");
        Notification_Reference.child(Notif_Id).updateChildren(Notif_map);
    }

    public static String encrypt(String value ) {
        if (value == null) { return null; }
        SecretKeySpec secretKeySpec = new SecretKeySpec(BuildConfig.ENCK.getBytes(), "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec("i_cb_en_ck_en_cp".getBytes()));
            byte[] values = cipher.doFinal(value.getBytes());
            return Base64.encodeToString(values, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
    public static  String decrypt(String value) {
        if (value == null) { return null; }
        try {
            byte[] values = Base64.decode(value, Base64.DEFAULT);
            SecretKeySpec secretKeySpec = new SecretKeySpec(BuildConfig.ENCK.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec("i_cb_en_ck_en_cp".getBytes()));
            return new String(cipher.doFinal(values));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
