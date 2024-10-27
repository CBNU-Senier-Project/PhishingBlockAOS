package com.example.phishingblock.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.phishingblock.MainActivity;
import com.example.phishingblock.R;

public class SplashActivity extends AppCompatActivity {

    private TextView logoText;
    private Handler handler;
    private int dotCount = 0;
    private boolean isAnimating = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 로고 텍스트 가져오기
        logoText = findViewById(R.id.logo_text);
        handler = new Handler();

        // 점 애니메이션 시작
        startDotAnimation();

        // 일정 시간 후 MainActivity로 이동
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 애니메이션 종료
                isAnimating = false;
                // MainActivity로 이동
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // SplashActivity 종료
            }
        }, 2000);  // 2초 동안 로딩 화면 표시
    }

    private void startDotAnimation() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (isAnimating) {
                    // 점 개수에 따라 텍스트를 업데이트
                    dotCount = (dotCount + 1) % 4; // 0, 1, 2, 3 반복
                    StringBuilder dots = new StringBuilder();
                    for (int i = 0; i < dotCount; i++) {
                        dots.append(".");
                    }
                    logoText.setText("PHISHING BLOCK" + dots.toString());

                    // 500ms마다 반복
                    handler.postDelayed(this, 300);
                }
            }
        };

        // 첫 번째 애니메이션 실행
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티 종료 시 애니메이션 정지
        isAnimating = false;
        handler.removeCallbacksAndMessages(null);
    }
}
