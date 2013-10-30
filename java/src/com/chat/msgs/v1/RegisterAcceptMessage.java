package com.chat.msgs.v1;

public class RegisterAcceptMessage {
    private final long userId;

    public RegisterAcceptMessage(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }
} 
