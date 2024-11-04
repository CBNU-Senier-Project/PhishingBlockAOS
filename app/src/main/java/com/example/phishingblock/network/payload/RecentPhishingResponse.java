package com.example.phishingblock.network.payload;

import com.google.gson.annotations.SerializedName;

public class RecentPhishingResponse {
    @SerializedName("phishingId")
    private int phishingId;

    @SerializedName("phishingType")
    private String phishingType;

    @SerializedName("value")
    private String value;

    @SerializedName("content")
    private String content;

    @SerializedName("createdAt")
    private String createdAt;

    // Getters and setters
    public int getPhishingId() {
        return phishingId;
    }

    public void setPhishingId(int phishingId) {
        this.phishingId = phishingId;
    }

    public String getPhishingType() {
        return phishingType;
    }

    public void setPhishingType(String phishingType) {
        this.phishingType = phishingType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
