package com.chat.msgs.v1;

public class LoginRejectMessage {
    private final int APIVersion;
    private final String UUID;

    public LoginRejectMessage(int APIVersion, String UUID) {
        this.APIVersion = APIVersion;
        this.UUID = UUID;
    }

    public int getAPIVersion() {
        return APIVersion;
    }

    public String getUUID() {
        return UUID;
    }
} 
