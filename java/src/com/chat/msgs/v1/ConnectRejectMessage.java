package com.chat.msgs.v1;

public class ConnectRejectMessage {
    private final String reason;

    public ConnectRejectMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
} 
