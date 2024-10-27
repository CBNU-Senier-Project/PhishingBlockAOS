package com.example.phishingblock.background;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.phishingblock.R;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private View overlayView;
    private boolean isOverlayAdded = false; // 뷰의 첨부 상태를 추적하는 플래그

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // 오버레이 레이아웃을 인플레이트
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_alert, null);

        // 버튼 및 텍스트 뷰 설정
        TextView alertText = overlayView.findViewById(R.id.alertText);
        alertText.setText("보이스 피싱 의심 대화가 감지되었습니다.");

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

        // 오버레이 레이아웃 파라미터 설정
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.x = 0;
        params.y = 0;
        params.gravity = android.view.Gravity.CENTER;

        try {
            windowManager.addView(overlayView, params);
            isOverlayAdded = true; // 뷰가 성공적으로 추가되었음을 표시
            Log.d("OverlayService", "Overlay View Added");
        } catch (Exception e) {
            Log.e("OverlayService", "Error adding overlay view: " + e.getMessage());
        }
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

}
