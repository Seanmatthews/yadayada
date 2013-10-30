package com.chat.msgs.v1;

public class LeftChatroomMessage {
    private final long chatroomId;
    private final long userId;

    public LeftChatroomMessage(long chatroomId, long userId) {
        this.chatroomId = chatroomId;
        this.userId = userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public long getUserId() {
        return userId;
    }
} 
