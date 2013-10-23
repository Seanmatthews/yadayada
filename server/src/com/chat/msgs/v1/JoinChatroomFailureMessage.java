package com.chat.msgs.v1;

public class JoinChatroomFailureMessage {
    private final long chatroomId;
    private final String reason;

    public JoinChatroomFailureMessage(long chatroomId, String reason) {
        this.chatroomId = chatroomId;
        this.reason = reason;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public String getReason() {
        return reason;
    }
} 
