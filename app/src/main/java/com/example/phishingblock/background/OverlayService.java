package com.example.phishingblock.background;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;

import com.example.phishingblock.R;

public class OverlayService extends Service {
    private static final String CHANNEL_ID = "overlay_service_channel";
    private WindowManager windowManager;
    private View overlayView;
    private boolean isOverlayAdded = false; // 뷰의 첨부 상태를 추적하는 플래그

    public static final String EXTRA_MESSAGE = "extra_message"; // 메시지를 위한 키
    public static final String EXTRA_LAYOUT_TYPE = "extra_layout_type"; // 추가된 부분: 레이아웃 타입

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // 기본 알림 설정
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Voice Phishing Alert")
                .setContentText("보이스 피싱 탐지 시스템이 활성화되었습니다.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(1, notification);

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String message = intent != null ? intent.getStringExtra(EXTRA_MESSAGE) : "알림 메시지";
        String layoutType = intent != null ? intent.getStringExtra(EXTRA_LAYOUT_TYPE) : "phishing";

        // 보이스피싱일 때와 일반 전화일 때 레이아웃을 다르게 설정
        if (overlayView != null && isOverlayAdded) {
            windowManager.removeView(overlayView); // 중복 방지 위해 제거
            isOverlayAdded = false;
        }

        // 레이아웃 타입에 따른 인플레이트
        if ("normal".equals(layoutType)) {
            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_alert_normal, null); // 일반 전화용 레이아웃
            // 텍스트 뷰에 전달받은 메시지 설정
            TextView alertText = overlayView.findViewById(R.id.alertText);
            alertText.setText(message);
        } else {
            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_alert_phishing, null); // 보이스피싱 의심 시 레이아웃
        }



        Button closeButton = overlayView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            removeOverlayView();
            stopSelf();
        });

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // dp 값을 px로 변환
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());

        // 오버레이 레이아웃 파라미터 설정
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width, // 300dp를 픽셀로 변환한 값
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.CENTER;
        try {
            windowManager.addView(overlayView, params);
            isOverlayAdded = true; // 뷰가 성공적으로 추가되었음을 표시
            Log.d("OverlayService", "Overlay View Added");
        } catch (Exception e) {
            Log.e("OverlayService", "Error adding overlay view: " + e.getMessage());
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeOverlayView();
    }

    private void removeOverlayView() {
        if (overlayView != null && isOverlayAdded) {
            try {
                windowManager.removeView(overlayView);
                isOverlayAdded = false;
                Log.d("OverlayService", "Overlay View Removed");
            } catch (IllegalArgumentException e) {
                Log.e("OverlayService", "Error removing overlay view: " + e.getMessage());
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Overlay Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
