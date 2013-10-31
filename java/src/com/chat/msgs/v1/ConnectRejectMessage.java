package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class ConnectRejectMessage implements Message {
    private final String reason;

    public ConnectRejectMessage(BinaryStream stream) throws IOException {
        this.reason = stream.readString();
    }

    public ConnectRejectMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           ConnectRejectMessage msg = this;
           stream.startWriting(1 + getStrLen(msg.getReason()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.ConnectReject.getValue());
        stream.writeString(getReason());
        stream.finishWriting();
    }
} 
