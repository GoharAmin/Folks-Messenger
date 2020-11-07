package codegradients.com.chatapp.helper_classes;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchHelpers;
import com.sinch.android.rtc.calling.CallNotificationResult;

import java.util.Map;

import codegradients.com.chatapp.Activities.ChatActivity;
import codegradients.com.chatapp.Activities.GroupChatActivity;
import codegradients.com.chatapp.Activities.MainActivity;
import codegradients.com.chatapp.R;

public class FirebaseMessageService extends FirebaseMessagingService {

    public static SinchClient sinchClient;

    public static String CHANNEL_ID = "Sinch Push Notification Channel";
    //NB: example purposes only! Implement proper storage/database in order to be able to create
    //nice notification with display name / picture / other data of the caller.
    private final String PREFERENCE_FILE = "com.sinch.android.rtc.sample.video.push.shared_preferences";
    SharedPreferences sharedPreferences;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.v("messageComing__", "Here");
        Map data = remoteMessage.getData();

        if (SinchHelpers.isSinchPushPayload(remoteMessage.getData())) {

            new ServiceConnection() {
                private Map payload;

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {

                    Context context = getApplicationContext();
                    sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);

                    if (payload != null) {
                        SinchService.SinchServiceInterface sinchService = (SinchService.SinchServiceInterface) service;
                        if (sinchService != null) {

                            Log.v("SInchCall___", "CallComing");

                            NotificationResult result = sinchService.relayRemotePushNotificationPayload(payload);
                            // handle result, e.g. show a notification or similar
                            // here is example for notifying user about missed/canceled call:
                            if (result.isValid() && result.isCall()) {
                                CallNotificationResult callResult = result.getCallResult();
                                if (callResult != null && result.getDisplayName() != null) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(callResult.getRemoteUserId(), result.getDisplayName());
                                    editor.commit();

                                    Log.v("SInchCall___", "Result valid");
                                }
                                if (callResult.isCallCanceled()) {
                                    String displayName = result.getDisplayName();
                                    if (displayName == null) {
                                        displayName = sharedPreferences.getString(callResult.getRemoteUserId(),"n/a");
                                    }

                                    Log.v("SInchCall___", "Missed Call");

                                    createMissedCallNotification(displayName != null && !displayName.isEmpty() ? displayName : callResult.getRemoteUserId());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        context.deleteSharedPreferences(PREFERENCE_FILE);
                                    }
                                }
                            } else {
                                Log.v("SInchCall___", "Missed Call BElow Above");
                                if (result.getCallResult().isCallCanceled()){
                                    String displayName = result.getDisplayName();
                                    if (displayName == null) {
                                        displayName = sharedPreferences.getString(result.getCallResult().getRemoteUserId(),"n/a");
                                    }

                                    Log.v("SInchCall___", "Missed Call BElow");

                                    createMissedCallNotification(displayName != null && !displayName.isEmpty() ? displayName : result.getCallResult().getRemoteUserId());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        context.deleteSharedPreferences(PREFERENCE_FILE);
                                    }
                                }
                            }
                        }
                    }
                    payload = null;
                    Log.v("SInchCall___", "Payload Null");
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.v("SInchCall___", "Service Disconnected");
                }

                public void relayMessageData(Map<String, String> data) {
                    payload = data;
                    Log.v("SInchCall___", "Relaying Data");
                    createNotificationChannel(NotificationManager.IMPORTANCE_MAX);
                    getApplicationContext().bindService(new Intent(getApplicationContext(), SinchService.class), this, BIND_AUTO_CREATE);
                }
            }.relayMessageData(data);

            Log.v("SInchCall___", "Here End");

        } else {
            // it's NOT Sinch message - process yourself
//            String Title=remoteMessage.getNotification().getTitle();
//            String Content=remoteMessage.getNotification().getBody();

            String type = remoteMessage.getData().get("type");

            Log.v("messageComing__", "Here : " + type);

            if (type.equals("scheduledMessageSingle")){
                //The message is scheduled. So handle it yourself

                Log.v("messageComing__", "Time Of Noti : " + remoteMessage.getData().get("timeForMessage") + "  Name: " + remoteMessage.getData().get("userName"));

                String receiverId = remoteMessage.getData().get("userId");
                String receiverName = remoteMessage.getData().get("userName");
                String receiverImage = remoteMessage.getData().get("userImage");
                long timeForMessage = Long.parseLong(remoteMessage.getData().get("timeForMessage"));
                String message = remoteMessage.getData().get("message");

                String nummForAlKey = String.valueOf(timeForMessage);
                int alarmkey = Integer.valueOf(nummForAlKey.substring(nummForAlKey.length() - 3));



                Intent intt = new Intent(FirebaseMessageService.this, AlarmReceiver.class);
                intt.putExtra("userId", receiverId);
                intt.putExtra("userName", remoteMessage.getData().get("userName"));
                intt.putExtra("userImage", receiverImage);
                intt.putExtra("message", message);
                intt.putExtra("type", "single");
                PendingIntent intent = PendingIntent.getBroadcast(FirebaseMessageService.this, alarmkey, intt, 0);

                AlarmManager alarmManager = (AlarmManager) FirebaseMessageService.this.getSystemService(Context.ALARM_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeForMessage, intent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeForMessage, intent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeForMessage, intent);
                }

                //alarmManager.set(AlarmManager.RTC_WAKEUP, timeForMessage, intent);
            } else if (type.equals("scheduledMessageGroup")) {

                Log.v("messageComing__", "Time Of Noti : " + remoteMessage.getData().get("timeForMessage") + "  Name: " + remoteMessage.getData().get("userName"));

                String receiverId = remoteMessage.getData().get("userId");
                String receiverName = remoteMessage.getData().get("userName");
                String receiverImage = remoteMessage.getData().get("userImage");
                long timeForMessage = Long.parseLong(remoteMessage.getData().get("timeForMessage"));
                String message = remoteMessage.getData().get("message");

                String nummForAlKey = String.valueOf(timeForMessage);
                int alarmkey = Integer.valueOf(nummForAlKey.substring(nummForAlKey.length() - 3));



                Intent intt = new Intent(FirebaseMessageService.this, AlarmReceiver.class);
                intt.putExtra("grpId", remoteMessage.getData().get("grpId"));
                intt.putExtra("grpName", remoteMessage.getData().get("grpName"));
                intt.putExtra("userName", remoteMessage.getData().get("userName"));
                intt.putExtra("message", message);
                intt.putExtra("type", "group");
                PendingIntent intent = PendingIntent.getBroadcast(FirebaseMessageService.this, alarmkey, intt, 0);

                AlarmManager alarmManager = (AlarmManager) FirebaseMessageService.this.getSystemService(Context.ALARM_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeForMessage, intent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeForMessage, intent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeForMessage, intent);
                }
            } else if (type.equals("messageRequest")) {

                Log.v("ReqReceived__", "HEre: " + remoteMessage.getData().get("message"));

                String personId = remoteMessage.getData().get("personId");
                String personName = remoteMessage.getData().get("personName");
                String personImage = remoteMessage.getData().get("personImage");
                String time = remoteMessage.getData().get("time");
                String messageToShow = "";

                if (remoteMessage.getData().get("message").equals("friendRequest")) {
                    messageToShow = "Has Sent You A Friend Request";
                } else if (remoteMessage.getData().get("message").equals("acceptedRequest")) {
                    messageToShow = "Has Accepted Your Friend Request";
                } else if (remoteMessage.getData().get("message").equals("declinedRequest")) {
                    messageToShow = "Has Declined Your Friend Request";
                }

                Intent resIntent = new Intent(FirebaseMessageService.this, ChatActivity.class);
                resIntent.putExtra("userId", personId);
                resIntent.putExtra("userName", personName);
                resIntent.putExtra("userImage", personImage);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(FirebaseMessageService.this);
                stackBuilder.addNextIntentWithParentStack(resIntent);
                PendingIntent resultIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(FirebaseMessageService.this, "1")
                        .setSmallIcon(R.drawable.holst_icon)
                        .setContentTitle(personName)
                        .setContentText(messageToShow)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(resultIntent);

                NotificationManager mNotificationManager = (NotificationManager) FirebaseMessageService.this.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("1"," ", NotificationManager.IMPORTANCE_DEFAULT);
                    mNotificationManager.createNotificationChannel(channel);
                }

                mNotificationManager.notify(0, mBuilder.build());
            } else if (type.equals("groupLeaving")) {
                String lefterName = remoteMessage.getData().get("lefterName");
                String groupId = remoteMessage.getData().get("groupId");
                String groupName = remoteMessage.getData().get("groupName");
                String messageToShow = "" + lefterName + " has left the group";

                Intent resIntent = new Intent(FirebaseMessageService.this, GroupChatActivity.class);
                resIntent.putExtra("grpName", groupName);
                resIntent.putExtra("grpId", groupId);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(FirebaseMessageService.this);
                stackBuilder.addNextIntentWithParentStack(resIntent);
                PendingIntent resultIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(FirebaseMessageService.this, "1")
                        .setSmallIcon(R.drawable.holst_icon)
                        .setContentTitle(groupName)
                        .setContentText(messageToShow)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(resultIntent);

                NotificationManager mNotificationManager = (NotificationManager) FirebaseMessageService.this.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("1"," ", NotificationManager.IMPORTANCE_DEFAULT);
                    mNotificationManager.createNotificationChannel(channel);
                }

                mNotificationManager.notify(0, mBuilder.build());
            } else if (type.equals("groupJoining")) {
                String addedName = remoteMessage.getData().get("addedName");
                String groupId = remoteMessage.getData().get("groupId");
                String groupName = remoteMessage.getData().get("groupName");
                String adderName = remoteMessage.getData().get("adderName");
                String messageToShow = "" + adderName + " has added you to the group";

                Intent resIntent = new Intent(FirebaseMessageService.this, GroupChatActivity.class);
                resIntent.putExtra("grpName", groupName);
                resIntent.putExtra("grpId", groupId);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(FirebaseMessageService.this);
                stackBuilder.addNextIntentWithParentStack(resIntent);
                PendingIntent resultIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(FirebaseMessageService.this, "1")
                        .setSmallIcon(R.drawable.holst_icon)
                        .setContentTitle(groupName)
                        .setContentText(messageToShow)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(resultIntent);

                NotificationManager mNotificationManager = (NotificationManager) FirebaseMessageService.this.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("1"," ", NotificationManager.IMPORTANCE_DEFAULT);
                    mNotificationManager.createNotificationChannel(channel);
                }

                mNotificationManager.notify(0, mBuilder.build());
            }
        }
    }

    private void createNotificationChannel(int importance) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        Uri uri = Uri.parse("android.resource"
                + "://" + getPackageName() + "/" + "raw/phone_loud1");
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Sinch";
            String description = "Incoming Sinch Push Notifications.";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(uri, audioAttributes);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void createMissedCallNotification(String userId) {

        createNotificationChannel(NotificationManager.IMPORTANCE_DEFAULT);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 112,
                new Intent(getApplicationContext(), MainActivity.class), 0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.holst_icon)
                        .setContentTitle("Missed call from:")
                        .setContentText(userId);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }
}
