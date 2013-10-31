package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class JoinChatroomMessage implements Message {
    private final long userId;
    private final long chatroomId;
    private final long latitude;
    private final long longitude;

    public JoinChatroomMessage(long userId, long chatroomId, long latitude, long longitude) {
        this.userId = userId;
        this.chatroomId = chatroomId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getUserId() {
        return userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           JoinChatroomMessage msg = this;
           stream.startWriting(1 + 8 + 8 + 8 + 8);
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.JoinChatroom.getValue());
        stream.writeLong(getUserId());
        stream.writeLong(getChatroomId());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());
        stream.finishWriting();
    }
} 
