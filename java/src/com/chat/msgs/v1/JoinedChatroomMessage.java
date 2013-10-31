package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class JoinedChatroomMessage implements Message {
    private final long chatroomId;
    private final long userId;
    private final String userHandle;

    public JoinedChatroomMessage(BinaryStream stream) throws IOException {
        this.chatroomId = stream.readLong();
        this.userId = stream.readLong();
        this.userHandle = stream.readString();
    }

    public JoinedChatroomMessage(long chatroomId, long userId, String userHandle) {
        this.chatroomId = chatroomId;
        this.userId = userId;
        this.userHandle = userHandle;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserHandle() {
        return userHandle;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           JoinedChatroomMessage msg = this;
           stream.startWriting(1 + 8 + 8 + getStrLen(msg.getUserHandle()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.JoinedChatroom.getValue());
        stream.writeLong(getChatroomId());
        stream.writeLong(getUserId());
        stream.writeString(getUserHandle());
        stream.finishWriting();
    }
} 
