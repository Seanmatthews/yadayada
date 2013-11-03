package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class SubmitMessageRejectMessage implements Message {
    private final long userId;
    private final long chatroomId;
    private final String reason;

    public SubmitMessageRejectMessage(BinaryStream stream) throws IOException {
        this.userId = stream.readLong();
        this.chatroomId = stream.readLong();
        this.reason = stream.readString();
    }

    public SubmitMessageRejectMessage(long userId, long chatroomId, String reason) {
        this.userId = userId;
        this.chatroomId = chatroomId;
        this.reason = reason;
    }

    public long getUserId() {
        return userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           SubmitMessageRejectMessage msg = this;
           stream.startWriting(1 + 8 + 8 + getStrLen(msg.getReason()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.SubmitMessageReject.getValue());
        stream.writeLong(getUserId());
        stream.writeLong(getChatroomId());
        stream.writeString(getReason());
        stream.finishWriting();
    }
} 
