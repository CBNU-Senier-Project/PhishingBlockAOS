package com.example.phishingblock.network.payload;


public class DetailPhishingDataResponse {
    private Long phishingId;        // Unique phishing ID
    private String phishingType;    // Phishing data type (ACCOUNT, PHONE, URL, etc.)
    private String value;           // The value being queried (e.g., account number, URL, etc.)
    private String content;         // Additional content or details about the phishing report
    private String createdAt;         // Timestamp of the phishing report creation

    // Constructor
    public DetailPhishingDataResponse(Long phishingId, String phishingType, String value, String content, String createdAt) {
        this.phishingId = phishingId;
        this.phishingType = phishingType;
        this.value = value;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getPhishingId() {
        return phishingId;
    }

    public void setPhishingId(Long phishingId) {
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
