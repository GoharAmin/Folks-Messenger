package codegradients.com.chatapp.helper_classes;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.video.VideoController;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.MissingPermissionException;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.Map;

import codegradients.com.chatapp.Activities.MainActivity;
import codegradients.com.chatapp.Activities.SinchScreens.IncomingCallScreenActivity;
import codegradients.com.chatapp.R;

import static codegradients.com.chatapp.Activities.SinchScreens.IncomingCallScreenActivity.ACTION_ANSWER;
import static codegradients.com.chatapp.Activities.SinchScreens.IncomingCallScreenActivity.ACTION_IGNORE;
import static codegradients.com.chatapp.Activities.SinchScreens.IncomingCallScreenActivity.EXTRA_ID;
import static codegradients.com.chatapp.Activities.SinchScreens.IncomingCallScreenActivity.MESSAGE_ID;
import static codegradients.com.chatapp.helper_classes.FirebaseMessageService.CHANNEL_ID;

public class SinchService extends Service {

    static final String APP_KEY = "73d554a7-9a4a-4067-b9c9-372ec6af9354";
    static final String APP_SECRET = "RgHibdzLEkOalBwCpB7gJw==";
    static final String ENVIRONMENT = "clientapi.sinch.com";

    public static final int MESSAGE_PERMISSIONS_NEEDED = 1;
    public static final String REQUIRED_PERMISSION = "REQUIRED_PESMISSION";
    public static final String MESSENGER = "MESSENGER";
    private Messenger messenger;

    public static final String CALL_ID = "CALL_ID";
    static final String TAG = SinchService.class.getSimpleName();

    private PersistedSettings mSettings;
    private SinchServiceInterface mSinchServiceInterface = new SinchServiceInterface();
    private SinchClient mSinchClient;

    private StartFailedListener mListener;

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = new PersistedSettings(getApplicationContext());
        attemptAutoStart();
    }

    private void attemptAutoStart() {
        if (messenger != null) {
            start();
        }
    }

    private void createClient(String username) {
        mSinchClient = Sinch.getSinchClientBuilder().context(getApplicationContext()).userId(username)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT).build();

        mSinchClient.setSupportCalling(true);
        mSinchClient.setSupportManagedPush(true);

        mSinchClient.addSinchClientListener(new MySinchClientListener());
        mSinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());
        mSinchClient.setPushNotificationDisplayName("User " + username);
    }

    @Override
    public void onDestroy() {
        if (mSinchClient != null && mSinchClient.isStarted()) {
            mSinchClient.terminateGracefully();
        }
        super.onDestroy();
    }

    private boolean hasUsername() {
        if (mSettings.getUsername().isEmpty()) {
            Log.e(TAG, "Can't start a SinchClient as no username is available!");
            return false;
        }
        return true;
    }

    private void createClientIfNecessary() {
        if (mSinchClient != null)
            return;
        if (!hasUsername()) {
            throw new IllegalStateException("Can't create a SinchClient as no username is available!");
        }
        createClient(mSettings.getUsername());
    }

    private void start() {
        boolean permissionsGranted = true;
        createClientIfNecessary();
        try {
            //mandatory checks
            mSinchClient.checkManifest();
            //auxiliary check
            if (getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                throw new MissingPermissionException(Manifest.permission.CAMERA);
            }
        } catch (MissingPermissionException e) {
            permissionsGranted = false;
            if (messenger != null) {
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString(REQUIRED_PERMISSION, e.getRequiredPermission());
                message.setData(bundle);
                message.what = MESSAGE_PERMISSIONS_NEEDED;
                try {
                    messenger.send(message);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        }
        if (permissionsGranted) {
            Log.d(TAG, "Starting SinchClient");
            try {
                mSinchClient.start();
            } catch (IllegalStateException e) {
                Log.w(TAG, "Can't start SinchClient - " + e.getMessage());
            }
        }
    }

    private void stop() {
        if (mSinchClient != null) {
            mSinchClient.terminateGracefully();
            mSinchClient = null;
        }
    }

    private boolean isStarted() {
        return (mSinchClient != null && mSinchClient.isStarted());
    }

    @Override
    public IBinder onBind(Intent intent) {
        messenger = intent.getParcelableExtra(MESSENGER);
        Log.v("SInchCall___", "Service In onBind");
        return mSinchServiceInterface;
    }

    public class SinchServiceInterface extends Binder {

        public Call callUserVideo(String userId) {
            return mSinchClient.getCallClient().callUserVideo(userId);
        }

        public Call callUserAudio(String userId) {
            return mSinchClient.getCallClient().callUser(userId);
        }

        public Call callUserConference(String userId) {
            return  mSinchClient.getCallClient().callConference(userId);
        }

        public String getUsername() { return mSettings.getUsername(); }

        public void setUsername(String username) { mSettings.setUsername(username);}

        public void retryStartAfterPermissionGranted() { SinchService.this.attemptAutoStart(); }

        public boolean isStarted() {
            return SinchService.this.isStarted();
        }

        public void startClient() { start(); }

        public void stopClient() {
            stop();
        }

        public void setStartListener(StartFailedListener listener) {
            mListener = listener;
        }

        public Call getCall(String callId) {
            return mSinchClient.getCallClient().getCall(callId);
        }



        public VideoController getVideoController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getVideoController();
        }

        public AudioController getAudioController() {
            if (!isStarted()) {
                return null;
            }
            return mSinchClient.getAudioController();
        }

        public NotificationResult relayRemotePushNotificationPayload(final Map payload) {
            if (!hasUsername()) {
                Log.e(TAG, "Unable to relay the push notification!");
                return null;
            }
            createClientIfNecessary();
            return mSinchClient.relayRemotePushNotificationPayload(payload);
        }
    }

    public interface StartFailedListener {

        void onStartFailed(SinchError error);

        void onStarted();
    }

    private class MySinchClientListener implements SinchClientListener {

        @Override
        public void onClientFailed(SinchClient client, SinchError error) {
            if (mListener != null) {
                mListener.onStartFailed(error);
            }

            Log.v("SInchCall___", "SInch Client Starting Failed");
            mSinchClient.terminate();
            mSinchClient = null;
        }

        @Override
        public void onClientStarted(SinchClient client) {
            Log.d(TAG, "SinchClient started");
            Log.v("SInchCall___", "SInch Client Started");
            if (mListener != null) {
                mListener.onStarted();
            }
        }

        @Override
        public void onClientStopped(SinchClient client) {
            Log.d(TAG, "SinchClient stopped");
        }

        @Override
        public void onLogMessage(int level, String area, String message) {
            switch (level) {
                case Log.DEBUG:
                    Log.d(area, message);
                    break;
                case Log.ERROR:
                    Log.e(area, message);
                    break;
                case Log.INFO:
                    Log.i(area, message);
                    break;
                case Log.VERBOSE:
                    Log.v(area, message);
                    break;
                case Log.WARN:
                    Log.w(area, message);
                    break;
            }
        }

        @Override
        public void onRegistrationCredentialsRequired(SinchClient client,
                ClientRegistration clientRegistration) {
        }
    }

    private class SinchCallClientListener implements CallClientListener {


        @Override
        public void onIncomingCall(CallClient callClient, Call call) {


            Log.d(TAG, "onIncomingCall: " + call.getCallId());
            Log.v("SInchCall___", "SInch Audio Call Coming");
            Intent intent = new Intent(SinchService.this, IncomingCallScreenActivity.class);
            intent.putExtra(EXTRA_ID, MESSAGE_ID);
            intent.putExtra(CALL_ID, call.getCallId());
            boolean inForeground = isAppOnForeground(getApplicationContext());

            DatabaseReference mDatabaseForGettingName = FirebaseDatabase.getInstance().getReference().child("users").child(call.getRemoteUserId());
            mDatabaseForGettingName.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //Toast.makeText(SinchService.this, "Call From " + dataSnapshot.child("userName").getValue(String.class), Toast.LENGTH_SHORT).show();

                    String callerName = HelperClass.decrypt(dataSnapshot.child("userName").getValue(String.class));
                    intent.putExtra("callerName", callerName);

//            if (!inForeground) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !inForeground) {
                        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(MESSAGE_ID, createIncomingCallNotification(call.getRemoteUserId(), intent, callerName));
                        mSinchClient.getCallClient().getCall(call.getCallId()).addCallListener(new CallListener() {
                            @Override
                            public void onCallProgressing(Call call) {

                                Log.v("CallTracking__", "CallProgrsssing");

                            }

                            @Override
                            public void onCallEstablished(Call call) {
                                Log.v("CallTracking__", "CallExtablished");

                                try {
                                    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(MESSAGE_ID);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onCallEnded(Call call) {
                                try {

                                    Log.v("CallTracking__", "Call Ended  " + call.getDetails().getDuration());

                                    if (call.getDetails().getDuration() == 0) {

                                        createNotificationChannelAgain();

                                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 112,
                                                new Intent(getApplicationContext(), MainActivity.class), 0);
                                        NotificationCompat.Builder mBuilder =
                                                new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_2nd)
                                                        .setSmallIcon(R.drawable.holst_icon)
                                                        .setContentTitle("Missed call from")
                                                        .setContentText(callerName);
                                        mBuilder.setContentIntent(contentIntent);
                                        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                                        mBuilder.setAutoCancel(true);
                                        NotificationManager mNotificationManager =
                                                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                        mNotificationManager.notify(1, mBuilder.build());
                                    }

                                    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(MESSAGE_ID);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onShouldSendPushNotification(Call call, List<PushPair> list) {
                                Log.v("CallTracking__", "CallPush Noti");

                            }
                        });

                    } else {
                        SinchService.this.startActivity(intent);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
        private Bitmap getBitmap(Context context, int resId) {
            int largeIconWidth = (int) context.getResources()
                    .getDimension(R.dimen.notification_large_icon_width);
            int largeIconHeight = (int) context.getResources()
                    .getDimension(R.dimen.notification_large_icon_height);
            Drawable d = context.getResources().getDrawable(resId);
            Bitmap b = Bitmap.createBitmap(largeIconWidth, largeIconHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            d.setBounds(0, 0, largeIconWidth, largeIconHeight);
            d.draw(c);
            return b;
        }

        private PendingIntent getPendingIntent(Intent intent, String action) {
            intent.setAction(action);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 111, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            return pendingIntent;
        }

        @TargetApi(29)
        private Notification createIncomingCallNotification(String userId, Intent fullScreenIntent, String callerName) {

            Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + getPackageName() + "/" + R.raw.phone_loud1);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 112, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setContentTitle("Incoming call")
                    .setContentText(callerName)
                    .setLargeIcon(getBitmap(getApplicationContext(), R.drawable.call_pressed))
                    .setSmallIcon(R.drawable.call_pressed)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(pendingIntent)
                    .setCategory(Notification.CATEGORY_CALL)
                    //.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setFullScreenIntent(pendingIntent, true)
                    .addAction(R.drawable.button_accept, "Answer",  getPendingIntent(fullScreenIntent, ACTION_ANSWER))
                    .addAction(R.drawable.button_decline, "Ignore", getPendingIntent(fullScreenIntent, ACTION_IGNORE))
                    .setOngoing(true).build();

            notification.flags = Notification.FLAG_INSISTENT;

            return notification;
        }
    }

    public static String CHANNEL_ID_2nd = "Sinch Push Missed Notification Channel";

    private void createNotificationChannelAgain() {
        Uri uri = Uri.parse("android.resource"
                + "://" + getPackageName() + "/" + "raw/phone_loud1");
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Sinch";
            String description = "Missed Call Notification";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_2nd, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            channel.setSound(null, null);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private class PersistedSettings {

        private SharedPreferences mStore;

        private static final String PREF_KEY = "Sinch";

        public PersistedSettings(Context context) {
            mStore = context.getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        }

        public String getUsername() {
            return mStore.getString("Username", "");
        }

        public void setUsername(String username) {
            SharedPreferences.Editor editor = mStore.edit();
            editor.putString("Username", username);
            editor.commit();
        }
    }
}
