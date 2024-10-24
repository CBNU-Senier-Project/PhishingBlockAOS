package com.example.phishingblock.network.payload;


public class PredictionResponse {

    private String prediction;

    // 기본 생성자
    public PredictionResponse() {}

    // 생성자
    public PredictionResponse(String prediction) {
        this.prediction = prediction;
    }

    // Getter
    public String getPrediction() {
        return prediction;
    }

    // Setter
    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }
}
