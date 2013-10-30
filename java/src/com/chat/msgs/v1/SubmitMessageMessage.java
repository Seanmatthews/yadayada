package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class SubmitMessageMessage implements Message {
    private final long userId;
    private final long chatroomId;
    private final String message;

    public SubmitMessageMessage(long userId, long chatroomId, String message) {
        this.userId = userId;
        this.chatroomId = chatroomId;
        this.message = message;
    }

    public long getUserId() {
        return userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           SubmitMessageMessage msg = this;
           stream.startWriting(1 + 8 + 8 + getStrLen(msg.getMessage()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.SubmitMessage.getValue());
        stream.writeLong(getUserId());
        stream.writeLong(getChatroomId());
        stream.writeString(getMessage());
        stream.finishWriting();
    }
} 
