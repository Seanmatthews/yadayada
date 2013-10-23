package com.chat.msgs.v1;

public class JoinChatroomRejectMessage {
    private final long chatroomId;
    private final String reason;

    public JoinChatroomRejectMessage(long chatroomId, String reason) {
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
