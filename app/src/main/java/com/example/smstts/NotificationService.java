package com.example.smstts;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import android.util.Log;
import android.widget.Toast;


public class NotificationService extends NotificationListenerService {
    public static final String TAG = "katextisok";
    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();


    }

    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getStringExtra("command").equals("clearall")) {
                Toast.makeText(context, "clearall", Toast.LENGTH_SHORT).show();
                NotificationService.this.cancelAllNotifications();
            } else if (intent.getStringExtra("command").equals("list")) {
                Toast.makeText(context, "list", Toast.LENGTH_SHORT).show();
                Intent i1 = new Intent("NOTIFICATION_LISTENER_SERVICE");
                i1.putExtra("notification_event", "====================");
                sendBroadcast(i1);
                int i = 1;

                for (StatusBarNotification sbn : NotificationService.this.getActiveNotifications()) {
                    Intent i2 = new Intent("NOTIFICATION_LISTENER_SERVICE");
                    i2.putExtra("notification_event", i + " " + sbn.getPackageName() + "\n");
                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new Intent("NOTIFICATION_LISTENER_EXAMPLE");
                i3.putExtra("notificaation_event", "==== Notification List====");
                sendBroadcast(i3);
            }


        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;

        int notificationCode = matchNotificationCode(sbn);

        if (notificationCode == InterceptedNotificationCode.KAKAO_SAMPLE_CODE) {
            Intent intent = new Intent("android.service.notification.NotificationListenerService");
            intent.putExtra("Notification Code", notificationCode);
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;
            String sampletitle = extras.getString(Notification.EXTRA_TITLE);
            CharSequence samplebody = extras.getCharSequence(Notification.EXTRA_TEXT);

            Log.i("NotificationListener", "샘플 푸시 제목 : " + sampletitle);
            Log.i("NotificationListener", "샘플 푸시 내용 : " + samplebody);

            intent.putExtra("sampleTitle", sampletitle);
            intent.putExtra("sampleBody", samplebody);
            sendBroadcast(intent);

        } else if (notificationCode == InterceptedNotificationCode.KAKAO_CODE) {
            Intent intent = new Intent("android.service.notification.NotificationListenerService");
            intent.putExtra("Notification Code", notificationCode);
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;

            String realTitle = extras.getString(Notification.EXTRA_TITLE);
            CharSequence realBody = extras.getCharSequence(Notification.EXTRA_TEXT);
            Log.i("NotificationListener", "푸시 제목 : " + realTitle);
            Log.i("NotificationListener", "푸시 내용 : " + realBody);
            if (realTitle == null || realBody == null) {

            } else if (realTitle != null || realBody != null) {

                intent.putExtra("realTitle", realTitle);
                intent.putExtra("realBody", realBody);
                sendBroadcast(intent);
            }


        }
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("kaTalk", "Notification Removed");
    }

    public static final class ApplicationPackageNames {
        public static final String KAKAO_SAMPLE_PACK_NAME = "com.kakao.sdk.sample";
        public static final String KAKAO_PACK_NAME = "com.kakao.talk";

    }

    public static final class InterceptedNotificationCode {
        public static final int KAKAO_SAMPLE_CODE = 1;
        public static final int KAKAO_CODE = 2;

    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        if (packageName.equals(ApplicationPackageNames.KAKAO_PACK_NAME)) {
            return (InterceptedNotificationCode.KAKAO_CODE);
        } else if (packageName.equals(ApplicationPackageNames.KAKAO_SAMPLE_PACK_NAME)) {
            return (InterceptedNotificationCode.KAKAO_SAMPLE_CODE);
        } else {
            return 0;
        }
    }
}
