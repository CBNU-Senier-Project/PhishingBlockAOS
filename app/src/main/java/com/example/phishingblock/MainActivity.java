package com.example.phishingblock;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PhoneNumberAdapter phoneNumberAdapter;
    private BottomNavigationView bottomNavigationView;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private AudioContentObserver audioObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = getFragmentForMenuItem(item);

                if (fragment != null) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, fragment);
                    fragmentTransaction.commit();
                    return true;
                }
                return false;
            }
        });

        // PhoneNumberAdapter를 초기화하고 데이터 세팅
        List<String> phoneNumbers = new ArrayList<>();  // 여기에 실제 데이터를 넣어야 합니다.
        phoneNumberAdapter = new PhoneNumberAdapter(phoneNumbers, new PhoneNumberAdapter.OnItemClickListener() {
            @Override
            public void onReportClick(String phoneNumber) {
                // 신고 버튼 클릭 시 처리
            }

            @Override
            public void onCheckClick(String phoneNumber) {
                // 조회 버튼 클릭 시 처리
            }
        }, this);
    }

    private Fragment getFragmentForMenuItem(MenuItem item) {
        if (item.getItemId() == R.id.nav_home) {
            return new HomeFragment();
        } else if (item.getItemId() == R.id.nav_groups) {
            return new GroupsFragment();
        } else if (item.getItemId() == R.id.nav_search) {
            return new SearchFragment();
        } else if (item.getItemId() == R.id.nav_setting) {
            return new SettingFragment();
        } else {
            return null;
        }
    }

    // PhoneNumberAdapter에 접근할 수 있는 getter 추가
    public PhoneNumberAdapter getPhoneNumberAdapter() {
        return phoneNumberAdapter;
    }

    private void requestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.VIBRATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        };

        // 권한이 부여되지 않았을 경우에만 요청
        boolean permissionRequired = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionRequired = true;
                Log.d("main",permission+"허용 안됨");
                break;
            }
        }

        if (permissionRequired) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        } else {
            // 모든 권한이 이미 부여됨
            startBackgroundProcesses();
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startBackgroundProcesses();
            }
        }
    }

    // 권한 부여 후 백그라운드에서 처리할 프로세스 시작
    private void startBackgroundProcesses() {
        // MediaStore를 감시하기 위한 ContentObserver 설정
        Handler handler = new Handler();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        audioObserver = new AudioContentObserver(handler,this);
        getContentResolver().registerContentObserver(uri, true, audioObserver);

        Log.d("main", "MediaStore 감시 시작: " + uri.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioObserver != null) {
            getContentResolver().unregisterContentObserver(audioObserver);
        }

    }

}
