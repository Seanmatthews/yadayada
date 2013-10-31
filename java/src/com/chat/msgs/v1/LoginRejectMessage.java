package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class LoginRejectMessage implements Message {
    private final String reason;

    public LoginRejectMessage(BinaryStream stream) throws IOException {
        this.reason = stream.readString();
    }

    public LoginRejectMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           LoginRejectMessage msg = this;
           stream.startWriting(1 + getStrLen(msg.getReason()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.LoginReject.getValue());
        stream.writeString(getReason());
        stream.finishWriting();
    }
} 
