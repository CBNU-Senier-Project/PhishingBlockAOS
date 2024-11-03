package com.example.phishingblock.background;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;


import androidx.core.app.NotificationCompat;

public class STTExecutor {
    private Context context;
    private String gcsUri;
    private static final String CHANNEL_ID = "stt_channel"; // 녹음 채널과 구분된 STT 채널 ID

    public STTExecutor(Context context, String gcsUri) {
        this.context = context;
        this.gcsUri = gcsUri;
        createNotificationChannel(); // Notification 채널 생성
    }

    public void startSTTTask() {
        new Thread(() -> {
            try {
                STTHelper sttHelper = new STTHelper(context);
                sttHelper.asyncRecognizeGcs(gcsUri);

                // STT 작업 성공 시 알림 표시
                showSTTNotification("STT 작업 완료", "STT 작업이 성공적으로 완료되었습니다.");

            } catch (Exception e) {
                Log.e("STTExecutor", "STT 작업 중 오류 발생", e);

                // STT 작업 실패 시 알림 표시
                showSTTNotification("STT 작업 오류", "STT 작업 중 오류가 발생했습니다.");
            }
        }).start();
    }

    private void createNotificationChannel() {
        // 안드로이드 8.0 이상에서 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "STT Notification",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("STT 작업 알림을 표시합니다.");
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showSTTNotification(String title, String content) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now) // 알림 아이콘 설정
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        if (notificationManager != null) {
            Notification notification = builder.build();
            notificationManager.notify(2, notification); // 알림 ID 설정
        }
    }

}
