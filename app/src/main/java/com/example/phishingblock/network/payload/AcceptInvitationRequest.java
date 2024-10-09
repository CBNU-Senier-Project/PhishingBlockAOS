package com.example.phishingblock.network.payload;

public class AcceptInvitationRequest {
    private String status;

    public AcceptInvitationRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
