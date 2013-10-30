package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class CreateChatroomMessage implements Message {
    private final long ownerId;
    private final String chatroomName;
    private final long latitude;
    private final long longitude;
    private final long radius;

    public CreateChatroomMessage(long ownerId, String chatroomName, long latitude, long longitude, long radius) {
        this.ownerId = ownerId;
        this.chatroomName = chatroomName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getChatroomName() {
        return chatroomName;
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
           CreateChatroomMessage msg = this;
           stream.startWriting(1 + 8 + getStrLen(msg.getChatroomName()) + 8 + 8 + 8);
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.CreateChatroom.getValue());
        stream.writeLong(getOwnerId());
        stream.writeString(getChatroomName());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());
        stream.writeLong(getRadius());
        stream.finishWriting();
    }
} 
