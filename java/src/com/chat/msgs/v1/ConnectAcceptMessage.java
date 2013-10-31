package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class ConnectAcceptMessage implements Message {
    private final int APIVersion;
    private final long globalChatId;
    private final String imageUploadUrl;
    private final String imageDownloadUrl;

    public ConnectAcceptMessage(BinaryStream stream) throws IOException {
        this.APIVersion = stream.readInt();
        this.globalChatId = stream.readLong();
        this.imageUploadUrl = stream.readString();
        this.imageDownloadUrl = stream.readString();
    }

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

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           ConnectAcceptMessage msg = this;
           stream.startWriting(1 + 4 + 8 + getStrLen(msg.getImageUploadUrl()) + getStrLen(msg.getImageDownloadUrl()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.ConnectAccept.getValue());
        stream.writeInt(getAPIVersion());
        stream.writeLong(getGlobalChatId());
        stream.writeString(getImageUploadUrl());
        stream.writeString(getImageDownloadUrl());
        stream.finishWriting();
    }
} 
