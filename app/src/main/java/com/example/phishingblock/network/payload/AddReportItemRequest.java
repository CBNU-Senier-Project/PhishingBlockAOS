package com.example.phishingblock.network.payload;

public class AddReportItemRequest {
    private String phishingType;  // 신고 유형 (ACCOUNT, PHONE, URL)
    private String value;         // 신고된 값 (예: 번호, URL, 계좌)
    private String content;       // 신고 내용

    public AddReportItemRequest(String phishingType, String value, String content) {
        this.phishingType = phishingType;
        this.value = value;
        this.content = content;
    }

    // Getter & Setter
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
}

