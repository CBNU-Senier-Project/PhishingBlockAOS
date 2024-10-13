package com.example.phishingblock.home;

public class Article {
    private String title;
    private String link;
    private String imageUrl;  // 이미지 URL 필드 추가

    public Article(String title, String link, String imageUrl) {
        this.title = title;
        this.link = link;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

