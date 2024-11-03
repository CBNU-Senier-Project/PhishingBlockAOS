package com.example.phishingblock.network.payload;

public class RegisterFCMTokenRequest {
    private long userId;
    private long groupId;
    private DeviceInfo deviceInfo;
    private boolean isNotiEnabled;

    public RegisterFCMTokenRequest(long userId, long groupId, String token, String deviceType, boolean isNotiEnabled) {
        this.userId = userId;
        this.groupId = groupId;
        this.deviceInfo = new DeviceInfo(token, deviceType);
        this.isNotiEnabled = isNotiEnabled;
    }

    public static class DeviceInfo {
        private String token;
        private String deviceType;

        public DeviceInfo(String token, String deviceType) {
            this.token = token;
            this.deviceType = deviceType;
        }
    }
}
