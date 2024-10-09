package com.example.phishingblock.network.payload;

public class InvitationResponse {
    private Long invitationId;
    private String groupName;
    private String senderName;

    public Long getInvitationId() {
        return invitationId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getSenderName() {
        return senderName;
    }
}
