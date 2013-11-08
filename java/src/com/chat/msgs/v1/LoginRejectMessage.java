package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class LoginRejectMessage implements Message {
    private final String reason;

    public LoginRejectMessage(ReadBuffer stream) {
        this.reason = stream.readString();
    }

    public LoginRejectMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.LoginReject.getValue());
        stream.writeString(getReason());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=LoginReject");
        builder.append(",Reason=").append(getReason());
        return builder.toString();        
    }
} 
