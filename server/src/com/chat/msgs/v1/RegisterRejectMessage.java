package com.chat.msgs.v1;

public class RegisterRejectMessage {
    private final String reason;

    public RegisterRejectMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
} 
