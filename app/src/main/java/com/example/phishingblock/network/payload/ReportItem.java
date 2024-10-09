package com.example.phishingblock.network.payload;

public class ReportItem {
    private Long phishingId;    // 피싱 아이템의 ID (API에서 사용)
    private String phishingType;  // 신고 유형 (ACCOUNT, PHONE, URL)
    private String value;        // 신고된 값 (예: 계좌번호, 전화번호, URL)
    private String content;      // 신고 내용
    private String time;         // 신고 시간 (UI용)

    public ReportItem(Long phishingId, String phishingType, String value, String content) {
        this.phishingId = phishingId;
        this.phishingType = phishingType;
        this.value = value;
        this.content = content;
        this.time = "";  // 기본값은 빈 문자열, 나중에 추가 가능
    }

    // 생성자 오버로딩 (time 제외)
    public ReportItem(String phishingType, String value, String content) {
        this(null, phishingType, value, content);
    }

    // Getter & Setter 메서드
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "ReportItem{" +
                "phishingId=" + phishingId +
                ", phishingType='" + phishingType + '\'' +
                ", value='" + value + '\'' +
                ", content='" + content + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
