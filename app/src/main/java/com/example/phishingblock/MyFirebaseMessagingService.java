package com.example.phishingblock;

import static java.security.AccessController.getContext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.phishingblock.background.TokenManager;
import com.example.phishingblock.background.UserManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.RegisterFCMTokenRequest;
import com.example.phishingblock.network.payload.UserProfileResponse;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "phishing_block_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // 서버에 새로운 토큰을 전송하여 관리
        loadGroupIdAndRegisterToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 메시지의 알림 데이터 확인
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            sendNotification(title, body);
        }
    }

    private void loadGroupIdAndRegisterToken(String token) {
        ApiService apiService = RetrofitClient.getApiService();

        // 사용자 프로필 정보 로드
        UserProfileResponse userProfile = UserManager.getUserProfile(this);
        if (userProfile == null) {
            Log.e("FCM", "User profile is not loaded.");
            return;
        }

        long userId = userProfile.getUserId();
        String accessToken = TokenManager.getAccessToken(this);  // 적절한 인증 토큰 가져오기

        // 그룹 ID를 로드하기 위해 API 호출
        apiService.getGroupIds(accessToken, userId).enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    long groupId = response.body().get(0);  // 첫 번째 그룹 ID 사용
                    registerFCMToken(userId, groupId, token);
                } else {
                    Log.e("FCM", "Failed to load group ID");
                }
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                Log.e("FCM", "Error loading group ID: " + t.getMessage());
            }
        });
    }

    private void registerFCMToken(long userId, long groupId, String token) {
        ApiService apiService = RetrofitClient.getApiService();

        // FCM 토큰 등록 요청 객체 생성
        RegisterFCMTokenRequest tokenRequest = new RegisterFCMTokenRequest(
                userId,
                groupId,
                token,
                "ANDROID",
                true
        );
        String accessToken = TokenManager.getAccessToken(this);
        // API 호출
        apiService.registerFCMToken(accessToken,tokenRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM", "Token registered successfully");
                } else {
                    Log.e("FCM", "Failed to register token");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FCM", "Error registering token: " + t.getMessage());
            }
        });
    }

    private void sendNotification(String title, String messageBody) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android Oreo 이상에서는 Notification Channel이 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Phishing Block Notifications";
            String description = "Notifications for phishing call alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        // 알림 빌더 생성
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // 알림 아이콘 설정
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 알림 표시
        notificationManager.notify(0, notificationBuilder.build());
    }
}
