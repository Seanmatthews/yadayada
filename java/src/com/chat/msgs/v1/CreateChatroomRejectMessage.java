package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class CreateChatroomRejectMessage implements Message {
    private final String chatroomName;
    private final String reason;

    public CreateChatroomRejectMessage(String chatroomName, String reason) {
        this.chatroomName = chatroomName;
        this.reason = reason;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           CreateChatroomRejectMessage msg = this;
           stream.startWriting(1 + getStrLen(msg.getChatroomName()) + getStrLen(msg.getReason()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.CreateChatroomReject.getValue());
        stream.writeString(getChatroomName());
        stream.writeString(getReason());
        stream.finishWriting();
    }
} 
