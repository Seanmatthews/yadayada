package com.chat.msgs.v1_3_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class InviteUserRejectMessage implements Message {
    private final String reason;

    public InviteUserRejectMessage(ReadBuffer stream) {
        this.reason = stream.readString();
    }

    public InviteUserRejectMessage(String reason) {
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
   
        stream.writeByte(MessageTypes.InviteUserReject.getValue());
        stream.writeString(getReason());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=InviteUserReject");
        builder.append(",Reason=").append(getReason());
        return builder.toString();        
    }
} 
