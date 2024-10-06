package com.example.phishingblock.network.payload;

import com.google.gson.annotations.SerializedName;

public class SignUpRequest {
    @SerializedName("userCertification")
    private UserCertification userCertification;

    @SerializedName("userInfo")
    private UserInfo userInfo;

    // Hardcoded userRole as "USER"
    @SerializedName("userRole")
    private String userRole = "USER"; // Default to USER role

    public SignUpRequest(UserCertification userCertification, UserInfo userInfo) {
        this.userCertification = userCertification;
        this.userInfo = userInfo;
    }

    public static class UserCertification {
        @SerializedName("email")
        private String email;

        @SerializedName("password")
        private String password;

        public UserCertification(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }

    public static class UserInfo {
        @SerializedName("nickname")
        private String nickname;

        @SerializedName("phnum")
        private String phnum;

        // Add dateOfBirth field
        @SerializedName("birthDate")
        private String birthDate;

        public UserInfo(String nickname, String phnum, String birthDate) {
            this.nickname = nickname;
            this.phnum = phnum;
            this.birthDate = birthDate;
        }

        public String getNickname() {
            return nickname;
        }

        public String getPhnum() {
            return phnum;
        }

        public String getBirthDate() {
            return birthDate;
        }
    }

    public UserCertification getUserCertification() {
        return userCertification;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public String getUserRole() {
        return userRole;
    }
}
