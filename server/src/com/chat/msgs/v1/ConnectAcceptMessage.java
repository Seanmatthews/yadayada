package com.chat.msgs.v1;

public class ConnectAcceptMessage {
    private final int APIVersion;
    private final long globalChatId;
    private final String imageUploadUrl;
    private final String imageDownloadUrl;

    public ConnectAcceptMessage(int APIVersion, long globalChatId, String imageUploadUrl, String imageDownloadUrl) {
        this.APIVersion = APIVersion;
        this.globalChatId = globalChatId;
        this.imageUploadUrl = imageUploadUrl;
        this.imageDownloadUrl = imageDownloadUrl;
    }

    public int getAPIVersion() {
        return APIVersion;
    }

    public long getGlobalChatId() {
        return globalChatId;
    }

    public String getImageUploadUrl() {
        return imageUploadUrl;
    }

    public String getImageDownloadUrl() {
        return imageDownloadUrl;
    }
} 
