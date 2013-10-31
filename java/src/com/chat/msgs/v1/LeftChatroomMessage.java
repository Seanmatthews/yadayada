package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class LeftChatroomMessage implements Message {
    private final long chatroomId;
    private final long userId;

    public LeftChatroomMessage(BinaryStream stream) throws IOException {
        this.chatroomId = stream.readLong();
        this.userId = stream.readLong();
    }

    public LeftChatroomMessage(long chatroomId, long userId) {
        this.chatroomId = chatroomId;
        this.userId = userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           LeftChatroomMessage msg = this;
           stream.startWriting(1 + 8 + 8);
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.LeftChatroom.getValue());
        stream.writeLong(getChatroomId());
        stream.writeLong(getUserId());
        stream.finishWriting();
    }
} 
