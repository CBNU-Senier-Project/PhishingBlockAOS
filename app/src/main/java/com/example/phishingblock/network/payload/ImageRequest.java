package com.example.phishingblock.network.payload;

public class ImageRequest {
    private String imagename;

    // 생성자
    public ImageRequest(String imagename) {
        this.imagename = imagename;
    }

    // Getter
    public String getImagename() {
        return imagename;
    }

    // Setter
    public void setImagename(String imagename) {
        this.imagename = imagename;
    }
}
