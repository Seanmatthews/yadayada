package com.chat.msgs.v1;

public class LoginRejectMessage {
    private final String reason;

    public LoginRejectMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
} 
