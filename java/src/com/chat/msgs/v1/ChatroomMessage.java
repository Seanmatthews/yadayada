package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class ChatroomMessage implements Message {
    private final long chatroomId;
    private final long chatroomOwnerId;
    private final String chatroomName;
    private final String chatroomOwnerHandle;
    private final long latitude;
    private final long longitude;
    private final long radius;

    public ChatroomMessage(long chatroomId, long chatroomOwnerId, String chatroomName, String chatroomOwnerHandle, long latitude, long longitude, long radius) {
        this.chatroomId = chatroomId;
        this.chatroomOwnerId = chatroomOwnerId;
        this.chatroomName = chatroomName;
        this.chatroomOwnerHandle = chatroomOwnerHandle;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public long getChatroomOwnerId() {
        return chatroomOwnerId;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public String getChatroomOwnerHandle() {
        return chatroomOwnerHandle;
    }

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public long getRadius() {
        return radius;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           ChatroomMessage msg = this;
           stream.startWriting(1 + 8 + 8 + getStrLen(msg.getChatroomName()) + getStrLen(msg.getChatroomOwnerHandle()) + 8 + 8 + 8);
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.Chatroom.getValue());
        stream.writeLong(getChatroomId());
        stream.writeLong(getChatroomOwnerId());
        stream.writeString(getChatroomName());
        stream.writeString(getChatroomOwnerHandle());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());
        stream.writeLong(getRadius());
        stream.finishWriting();
    }
} 
