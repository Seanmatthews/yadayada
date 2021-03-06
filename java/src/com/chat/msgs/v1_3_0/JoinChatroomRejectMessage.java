package com.chat.msgs.v1_3_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class JoinChatroomRejectMessage implements Message {
    private final long chatroomId;
    private final String reason;

    public JoinChatroomRejectMessage(ReadBuffer stream) {
        this.chatroomId = stream.readLong();
        this.reason = stream.readString();
    }

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
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.JoinChatroomReject.getValue());
        stream.writeLong(getChatroomId());
        stream.writeString(getReason());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=JoinChatroomReject");
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",Reason=").append(getReason());
        return builder.toString();        
    }
} 
