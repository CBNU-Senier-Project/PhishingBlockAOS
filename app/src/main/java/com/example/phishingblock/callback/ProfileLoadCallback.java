package com.example.phishingblock.callback;

import com.example.phishingblock.network.payload.UserProfileResponse;

public interface ProfileLoadCallback {
    void onProfileLoaded(UserProfileResponse userProfile);
    void onProfileLoadFailed(String errorMessage);
}
