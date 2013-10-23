package com.chat.msgs.v1;

public class LoginAcceptMessage {
    private final String reason;

    public LoginAcceptMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
} 
