package com.example.phishingblock.network.payload;

public class CallDialogueRequest {

    private String dialogue;
    private String call_time;

    // 생성자
    public CallDialogueRequest(String dialogue, String call_time) {
        this.dialogue = dialogue;
        this.call_time = call_time;
    }

    // Getter 및 Setter 메서드
    public String getDialogue() {
        return dialogue;
    }

    public void setDialogue(String dialogue) {
        this.dialogue = dialogue;
    }

    public String getCall_time() {
        return call_time;
    }

    public void setCall_time(String call_time) {
        this.call_time = call_time;
    }
}
