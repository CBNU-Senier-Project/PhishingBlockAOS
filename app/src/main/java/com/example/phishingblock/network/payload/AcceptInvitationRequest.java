package com.example.phishingblock.network.payload;

public class AcceptInvitationRequest {
    private String status;  // 초대 상태 (ACCEPTED 또는 REJECTED)

    public AcceptInvitationRequest(String status) {
        this.status = status;
    }

    // Getter & Setter
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
