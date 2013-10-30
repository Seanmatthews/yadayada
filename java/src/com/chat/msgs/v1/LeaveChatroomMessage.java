package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class LeaveChatroomMessage implements Message {
    private final long userId;
    private final long chatroomId;

    public LeaveChatroomMessage(long userId, long chatroomId) {
        this.userId = userId;
        this.chatroomId = chatroomId;
    }

    public long getUserId() {
        return userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           LeaveChatroomMessage msg = this;
           stream.startWriting(1 + 8 + 8);
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.LeaveChatroom.getValue());
        stream.writeLong(getUserId());
        stream.writeLong(getChatroomId());
        stream.finishWriting();
    }
} 
