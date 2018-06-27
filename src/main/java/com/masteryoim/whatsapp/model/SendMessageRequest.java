package com.masteryoim.whatsapp.model;

public class SendMessageRequest {
    public final String phoneNumber;
    public final String message;
    public final String groupName;

    public SendMessageRequest(String phoneNumber, String groupName, String message) {
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.groupName = groupName;
    }

    static public SendMessageRequest buildRequestByPhoneNumber(String phoneNumber, String message) {
        return new SendMessageRequest(phoneNumber, null, message);
    }

    static public SendMessageRequest buildRequestByGroupName(String groupName, String message) {
        return new SendMessageRequest(null, groupName, message);
    }
}
