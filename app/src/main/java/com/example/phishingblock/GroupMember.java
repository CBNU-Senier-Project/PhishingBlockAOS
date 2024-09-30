package com.example.phishingblock;

public class GroupMember {
    private String name;
    private String profileImageUrl; // 이미지 URL을 저장하는 필드

    public GroupMember(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl; // URL로 수정
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
