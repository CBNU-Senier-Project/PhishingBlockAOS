package com.example.phishingblock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.widget.Toast;

public class NotificationUtils {

    private static final String CHANNEL_ID = "recording_channel";

    public static void showRecordingNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 안드로이드 8.0 이상에서 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recording Notification",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("녹음 시작 알림을 표시합니다.");

            showToast(context, "통화 중 녹음을 시작하세요.");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("녹음 시작")
                .setContentText("통화 중 녹음을 시작하세요.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            Notification notification = builder.build();
            notificationManager.notify(1, notification);
        }

    }
    private static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
