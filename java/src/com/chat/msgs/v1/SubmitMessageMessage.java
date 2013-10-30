package com.chat.msgs.v1;

public class SubmitMessageMessage {
    private final long userId;
    private final long chatroomId;
    private final String message;

    public SubmitMessageMessage(long userId, long chatroomId, String message) {
        this.userId = userId;
        this.chatroomId = chatroomId;
        this.message = message;
    }

    public long getUserId() {
        return userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public String getMessage() {
        return message;
    }
} 
