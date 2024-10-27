package com.example.phishingblock.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phishingblock.MainActivity;
import com.example.phishingblock.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 일정 시간 후 MainActivity로 이동
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // MainActivity로 이동
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();  // SplashActivity 종료
            }
        }, 2000);  // 3초 동안 로딩 화면 표시
    }
}
