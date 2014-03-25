package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class RegisterRejectMessage implements Message {
    private final String reason;

    public RegisterRejectMessage(ReadBuffer stream) {
        this.reason = stream.readString();
    }

    public RegisterRejectMessage(String reason) {
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
   
        stream.writeByte(MessageTypes.RegisterReject.getValue());
        stream.writeString(getReason());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=RegisterReject");
        builder.append(",Reason=").append(getReason());
        return builder.toString();        
    }
} 
