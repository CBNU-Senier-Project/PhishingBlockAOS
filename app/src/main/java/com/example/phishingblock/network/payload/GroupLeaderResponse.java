package com.example.phishingblock.network.payload;

public class GroupLeaderResponse {
    private String nickname;
    private String phnum;

    // Getters and setters
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
