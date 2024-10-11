package com.example.phishingblock.network.payload;

public class InviteMemberRequest {
    private String receiverPhoneNumber;

    public InviteMemberRequest(String receiverPhoneNumber) {
        this.receiverPhoneNumber = receiverPhoneNumber;
    }

    // Getter & Setter
    public String getReceiverPhoneNumber() {
        return receiverPhoneNumber;
    }

    public void setReceiverPhoneNumber(String receiverPhoneNumber) {
        this.receiverPhoneNumber = receiverPhoneNumber;
    }
}
