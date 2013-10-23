package com.chat.msgs.v1;

public class LoginAcceptMessage {
    private final long userId;

    public LoginAcceptMessage(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }
} 
