package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class JoinChatroomRejectMessage implements Message {
    private final long chatroomId;
    private final String reason;

    public JoinChatroomRejectMessage(long chatroomId, String reason) {
        this.chatroomId = chatroomId;
        this.reason = reason;
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
           JoinChatroomRejectMessage msg = this;
           stream.startWriting(1 + 8 + getStrLen(msg.getReason()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.JoinChatroomReject.getValue());
        stream.writeLong(getChatroomId());
        stream.writeString(getReason());
        stream.finishWriting();
    }
} 
