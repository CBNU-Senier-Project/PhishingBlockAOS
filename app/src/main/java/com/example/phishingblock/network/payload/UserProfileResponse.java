package com.example.phishingblock.network.payload;

public class UserProfileResponse {
    private long userId;
    private UserInfo userInfo;

    // 기본 생성자
    public UserProfileResponse() {
    }

    // 전체 필드를 포함한 생성자
    public UserProfileResponse(long userId, UserInfo userInfo) {
        this.userId = userId;
        this.userInfo = userInfo;
    }

    // Getter와 Setter
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    // UserInfo 내부 클래스
    public static class UserInfo {
        private String nickname;
        private String phnum;

        // 기본 생성자
        public UserInfo() {
        }

        // 전체 필드를 포함한 생성자
        public UserInfo(String nickname, String phnum) {
            this.nickname = nickname;
            this.phnum = phnum;
        }

        // Getter와 Setter
        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getPhnum() {
            return phnum;
        }

        public void setPhnum(String phnum) {
            this.phnum = phnum;
        }
    }
}
