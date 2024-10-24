package com.example.phishingblock.network.payload;

public class UserProfileRequest {

    private UserInfo userInfo;

    public UserProfileRequest(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public static class UserInfo {
        private String nickname;
        private String phnum;

        public UserInfo(String nickname, String phnum) {
            this.nickname = nickname;
            this.phnum = phnum;
        }

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
