package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class ChatroomMessage implements Message {
    private final long chatroomId;
    private final long chatroomOwnerId;
    private final String chatroomName;
    private final String chatroomOwnerHandle;
    private final long latitude;
    private final long longitude;
    private final long radius;

    public ChatroomMessage(ReadBuffer stream) {
        this.chatroomId = stream.readLong();
        this.chatroomOwnerId = stream.readLong();
        this.chatroomName = stream.readString();
        this.chatroomOwnerHandle = stream.readString();
        this.latitude = stream.readLong();
        this.longitude = stream.readLong();
        this.radius = stream.readLong();
    }

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
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        stream.advance(2);
   
        stream.writeByte(MessageTypes.Chatroom.getValue());
        stream.writeLong(getChatroomId());
        stream.writeLong(getChatroomOwnerId());
        stream.writeString(getChatroomName());
        stream.writeString(getChatroomOwnerHandle());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());
        stream.writeLong(getRadius());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }
} 
