package com.example.phishingblock;

public class ReportDetail {
    private String content;  // 신고 내용
    private String timestamp;  // 신고 시각

    public ReportDetail(String content, String timestamp) {
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
