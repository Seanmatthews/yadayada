package com.chat.msgs.v1;

public class ConnectAcceptMessage {
    private final int APIVersion;
    private final long globalChatId;

    public ConnectAcceptMessage(int APIVersion, long globalChatId) {
        this.APIVersion = APIVersion;
        this.globalChatId = globalChatId;
    }

    public int getAPIVersion() {
        return APIVersion;
    }

    public long getGlobalChatId() {
        return globalChatId;
    }
} 
