package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class RegisterRejectMessage implements Message {
    private final String reason;

    public RegisterRejectMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           RegisterRejectMessage msg = this;
           stream.startWriting(1 + getStrLen(msg.getReason()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.RegisterReject.getValue());
        stream.writeString(getReason());
        stream.finishWriting();
    }
} 
