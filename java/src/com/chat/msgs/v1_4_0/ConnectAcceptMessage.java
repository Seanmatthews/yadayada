package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class ConnectAcceptMessage implements Message {
    private final int APIVersion;
    private final long globalChatId;
    private final String imageUploadUrl;
    private final String imageDownloadUrl;
    private final short heartbeatInterval;

    public ConnectAcceptMessage(ReadBuffer stream) {
        this.APIVersion = stream.readInt();
        this.globalChatId = stream.readLong();
        this.imageUploadUrl = stream.readString();
        this.imageDownloadUrl = stream.readString();
        this.heartbeatInterval = stream.readShort();
    }

    public ConnectAcceptMessage(int APIVersion, long globalChatId, String imageUploadUrl, String imageDownloadUrl, short heartbeatInterval) {
        this.APIVersion = APIVersion;
        this.globalChatId = globalChatId;
        this.imageUploadUrl = imageUploadUrl;
        this.imageDownloadUrl = imageDownloadUrl;
        this.heartbeatInterval = heartbeatInterval;
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

    public short getHeartbeatInterval() {
        return heartbeatInterval;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.ConnectAccept.getValue());
        stream.writeInt(getAPIVersion());
        stream.writeLong(getGlobalChatId());
        stream.writeString(getImageUploadUrl());
        stream.writeString(getImageDownloadUrl());
        stream.writeShort(getHeartbeatInterval());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=ConnectAccept");
        builder.append(",APIVersion=").append(getAPIVersion());
        builder.append(",GlobalChatId=").append(getGlobalChatId());
        builder.append(",ImageUploadUrl=").append(getImageUploadUrl());
        builder.append(",ImageDownloadUrl=").append(getImageDownloadUrl());
        builder.append(",HeartbeatInterval=").append(getHeartbeatInterval());
        return builder.toString();        
    }
} 
