package com.example.phishingblock.network.payload;

public class LoginRequest {
    private UserCertification userCertification;

    public LoginRequest(String email, String password) {
        this.userCertification = new UserCertification(email, password);
    }

    public static class UserCertification {
        private String email;
        private String password;

        public UserCertification(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}
