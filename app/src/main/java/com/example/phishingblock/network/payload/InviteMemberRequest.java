package com.example.phishingblock.network.payload;

public class InviteMemberRequest {
    private Long receiverId;

    public InviteMemberRequest(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getReceiverId() {
        return receiverId;
    }
}
