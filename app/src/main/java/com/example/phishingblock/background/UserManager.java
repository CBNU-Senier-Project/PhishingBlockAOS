package com.example.phishingblock.background;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.phishingblock.network.payload.UserProfileResponse;

public class UserManager {
    private static UserProfileResponse userProfile;

    // 유저 프로필 저장 메서드
    public static void saveUserProfile(Context context, UserProfileResponse profile) {
        userProfile = profile;

        // 필요하다면 SharedPreferences에 저장하여 앱 종료 후에도 유지 가능
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("userId", profile.getUserId());
        editor.putString("nickname", profile.getUserInfo().getNickname());
        editor.putString("phnum", profile.getUserInfo().getPhnum());
        editor.apply();
    }

    // 유저 프로필 반환 메서드
    public static UserProfileResponse getUserProfile(Context context) {
        if (userProfile == null) {
            // 앱이 재시작된 경우 SharedPreferences에서 프로필 불러오기
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            long userId = sharedPreferences.getLong("userId", -1);
            String nickname = sharedPreferences.getString("nickname", null);
            String phnum = sharedPreferences.getString("phnum", null);

            if (userId != -1 && nickname != null && phnum != null) {
                UserProfileResponse.UserInfo userInfo = new UserProfileResponse.UserInfo(nickname, phnum);
                userProfile = new UserProfileResponse(userId, userInfo);
            }
        }
        return userProfile;
    }
}

