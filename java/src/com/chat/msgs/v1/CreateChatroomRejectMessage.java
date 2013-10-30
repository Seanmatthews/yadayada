package com.chat.msgs.v1;

public class CreateChatroomRejectMessage {
    private final String chatroomName;
    private final String reason;

    public CreateChatroomRejectMessage(String chatroomName, String reason) {
        this.chatroomName = chatroomName;
        this.reason = reason;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public String getReason() {
        return reason;
    }
} 
