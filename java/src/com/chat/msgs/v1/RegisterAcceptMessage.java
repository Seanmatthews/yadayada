package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class RegisterAcceptMessage implements Message {
    private final long userId;

    public RegisterAcceptMessage(BinaryStream stream) throws IOException {
        this.userId = stream.readLong();
    }

    public RegisterAcceptMessage(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           RegisterAcceptMessage msg = this;
           stream.startWriting(1 + 8);
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.RegisterAccept.getValue());
        stream.writeLong(getUserId());
        stream.finishWriting();
    }
} 
