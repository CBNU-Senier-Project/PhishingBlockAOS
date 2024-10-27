package com.example.phishingblock.network.payload;

public class NewsResponse {

    private String title;
    private String content;
    private String linkUrl;
    private String imageUrl;

    // 기본 생성자
    public NewsResponse() {}

    // 생성자
    public NewsResponse(String title, String content, String linkUrl, String imageUrl) {
        this.title = title;
        this.content = content;
        this.linkUrl = linkUrl;
        this.imageUrl = imageUrl;
    }

    // Getter 및 Setter 메서드
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
