package com.example.phishingblock.network.payload;

public class SearchPhishingDataRequest {
    private String phishingType;  // 검색할 피싱 데이터의 유형 (ACCOUNT, PHONE, URL)
    private String value;         // 검색할 값 (예: 의심되는 번호, URL, 계좌)
    private String content;       // 관련 내용

    public SearchPhishingDataRequest(String phishingType, String value, String content) {
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
