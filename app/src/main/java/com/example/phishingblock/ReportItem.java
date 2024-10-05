package com.example.phishingblock;

public class ReportItem {
    private String item;  // 의심되는 번호, URL, 계좌
    private String type;  // 신고 유형
    private String time;  // 신고 시간
    private String content;  // 신고 내용

    public ReportItem(String item, String type, String time, String content) {
        this.item = item;
        this.type = type;
        this.time = time;
        this.content = content;
    }

    // Getter & Setter 메서드
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
