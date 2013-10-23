package com.chat.msgs.v1;

public class LeaveChatroomMessage {
    private final long userId;
    private final long chatroomId;

    public LeaveChatroomMessage(long userId, long chatroomId) {
        this.userId = userId;
        this.chatroomId = chatroomId;
    }

    public long getUserId() {
        return userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }
} 
