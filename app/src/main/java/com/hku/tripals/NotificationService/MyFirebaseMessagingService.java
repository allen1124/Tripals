package com.hku.tripals.NotificationService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hku.tripals.ui.message.MessageActivity;

import androidx.core.app.NotificationCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onMessageReceived (RemoteMessage remoteMessaging){
        super.onMessageReceived(remoteMessaging);

        Log.d(TAG, "data received, ready to create notification...");
        Log.d(TAG, "remote message data: "+remoteMessaging.getData().toString());


        String sent = remoteMessaging.getData().get("sent");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "sender: "+sent);
        if (firebaseUser!= null && sent.equals(firebaseUser.getUid())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendOreoNotification(remoteMessaging);
            } else {
                sendNotification(remoteMessaging);
            }
        }
    }

    private void sendOreoNotification(RemoteMessage remoteMessage){
        Log.d(TAG, "send Oreo notification accessiblee");

        String chat_id = remoteMessage.getData().get("chat_id");
        String chat_name = remoteMessage.getData().get("chat_name");
        String chat_icon = remoteMessage.getData().get("chat_icon");
        String chat_type = remoteMessage.getData().get("chat_type");
        String chat_participants = remoteMessage.getData().get("chat_participants");

        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        int j = Integer.parseInt(chat_id.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);

        intent.putExtra("Chat_Id", chat_id);
        intent.putExtra("Chat_Name", chat_name);
        intent.putExtra("Chat_Icon", chat_icon);
        intent.putExtra("type", chat_type);
        intent.putExtra("participants", chat_participants);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound,  icon);

        int i = 0;
        if (j > 0){
            i=j;
        }
        oreoNotification.getManager().notify(i, builder.build());
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Log.d(TAG, "send notification accessible");
        String chat_id = remoteMessage.getData().get("chat_id");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        int j = Integer.parseInt(chat_id.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("eventID", chat_id);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0){
            i=j;
        }

        noti.notify(i, builder.build());
    }


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, token);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        if (user != null){
            updateToken(refreshToken);
        }
    }

    private void updateToken(String refreshToken ){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("tokens");
        Token token = new Token(refreshToken);
        reference.child(firebaseUser.getUid()).setValue(token);
        Log.d(TAG, "update Token works");
    }

}
