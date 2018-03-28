package com.masteryoim.whatsapp.model;

public class SendMessageRequest {
    public final String phoneNumber;
    public final String message;

    public SendMessageRequest(String phoneNumber, String message) {
        this.phoneNumber = phoneNumber;
        this.message = message;
    }
}
