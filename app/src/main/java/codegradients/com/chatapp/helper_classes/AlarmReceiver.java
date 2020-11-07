package codegradients.com.chatapp.helper_classes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import codegradients.com.chatapp.Activities.ChatActivity;
import codegradients.com.chatapp.Activities.GroupChatActivity;
import codegradients.com.chatapp.R;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.v("messageComing__", "Alarm Receiver");

        if (intent.getStringExtra("type").equals("single")){
            Intent resIntent = new Intent(context, ChatActivity.class);
            resIntent.putExtra("userId", intent.getStringExtra("userId"));
            resIntent.putExtra("userName", intent.getStringExtra("userName"));
            resIntent.putExtra("userImage", intent.getStringExtra("userImage"));

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(resIntent);
            PendingIntent resultIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "1")
                    .setSmallIcon(R.drawable.holst_icon)
                    .setContentTitle(intent.getStringExtra("userName"))
                    .setContentText(intent.getStringExtra("message"))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(resultIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("1"," ", NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationManager.createNotificationChannel(channel);
            }

            mNotificationManager.notify(0, mBuilder.build());
        } else {
            Intent resIntent = new Intent(context, GroupChatActivity.class);
            resIntent.putExtra("grpId", intent.getStringExtra("grpId"));
            resIntent.putExtra("grpName", intent.getStringExtra("grpName"));
//            resIntent.putExtra("userImage", intent.getStringExtra("userImage"));

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(resIntent);
            PendingIntent resultIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "1")
                    .setSmallIcon(R.drawable.holst_icon)
                    .setContentTitle(intent.getStringExtra("grpName"))
                    .setContentText(intent.getStringExtra("userName") + ": " + intent.getStringExtra("message"))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(resultIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("1"," ", NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationManager.createNotificationChannel(channel);
            }

            mNotificationManager.notify(0, mBuilder.build());
        }
    }
}
