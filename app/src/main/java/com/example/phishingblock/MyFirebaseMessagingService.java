package com.example.phishingblock;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.phishingblock.background.TokenManager;
import com.example.phishingblock.background.UserManager;
import com.example.phishingblock.callback.GroupIdLoadCallback;
import com.example.phishingblock.callback.ProfileLoadCallback;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.GroupMemberResponse;
import com.example.phishingblock.network.payload.RegisterFCMTokenRequest;
import com.example.phishingblock.network.payload.UserProfileResponse;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "phishing_block_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        loadGroupIdAndRegisterToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String targetUserId = data.get("userId");
            String title = data.get("title");
            String body = data.get("body");

            Log.d("debug",targetUserId+body);

            getNicknameFromGroup(targetUserId, nickname -> sendNotification(title, nickname + "님에게 보이스피싱 의심 전화가 감지되었습니다."));
        }
    }

    private void getNicknameFromGroup(String targetUserId, NicknameCallback callback) {
        String token = TokenManager.getAccessToken(this);

        loadUserProfile(token, new ProfileLoadCallback() {
            @Override
            public void onProfileLoaded(UserProfileResponse userProfile) {
                long userId = userProfile.getUserId();

                loadGroupId(userId, token, groupId -> loadGroupMembers(groupId, targetUserId, callback));
            }

            @Override
            public void onProfileLoadFailed(String errorMessage) {
                Log.e("ProfileLoad", "User profile load failed: " + errorMessage);
                callback.onNicknameLoaded("그룹원"); // 오류 발생 시 기본 값 반환
            }
        });
    }

    private void loadGroupMembers(long groupId, String targetUserId, NicknameCallback callback) {
        ApiService apiService = RetrofitClient.getApiService();
        String token = TokenManager.getAccessToken(this);

        apiService.getGroupMembers(token, groupId).enqueue(new Callback<List<GroupMemberResponse>>() {
            @Override
            public void onResponse(Call<List<GroupMemberResponse>> call, Response<List<GroupMemberResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (GroupMemberResponse member : response.body()) {
                        if (targetUserId.equals(String.valueOf(member.getUserId()))) {
                            callback.onNicknameLoaded(member.getName());
                            return;
                        }
                    }
                    callback.onNicknameLoaded("그룹원");
                } else {
                    callback.onNicknameLoaded("Error Loading Nickname");
                }
            }

            @Override
            public void onFailure(Call<List<GroupMemberResponse>> call, Throwable t) {
                callback.onNicknameLoaded("Error Loading Nickname");
            }
        });
    }

    private void loadUserProfile(String token, ProfileLoadCallback callback) {
        UserProfileResponse userProfile = UserManager.getUserProfile(this);
        if (userProfile != null) {
            callback.onProfileLoaded(userProfile);
        } else {
            callback.onProfileLoadFailed("유저 프로필을 불러오지 못했습니다.");
        }
    }

    private void loadGroupId(long creatorId, String token, GroupIdLoadCallback callback) {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.getGroupIds(token, creatorId).enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onGroupIdLoaded(response.body().get(0));
                }
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                // Handle failure
            }
        });
    }

    private void loadGroupIdAndRegisterToken(String token) {
        ApiService apiService = RetrofitClient.getApiService();
        UserProfileResponse userProfile = UserManager.getUserProfile(this);
        if (userProfile == null) {
            Log.e("FCM", "User profile is not loaded.");
            return;
        }

        long userId = userProfile.getUserId();
        String accessToken = TokenManager.getAccessToken(this);

        apiService.getGroupIds(accessToken, userId).enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    long groupId = response.body().get(0);
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
        RegisterFCMTokenRequest tokenRequest = new RegisterFCMTokenRequest(userId, groupId, token, "ANDROID", true);
        String accessToken = TokenManager.getAccessToken(this);

        apiService.registerFCMToken(accessToken, tokenRequest).enqueue(new Callback<Void>() {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Phishing Block Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for phishing call alerts");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private interface NicknameCallback {
        void onNicknameLoaded(String nickname);
    }
}
