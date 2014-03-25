package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class SubmitMessageRejectMessage implements Message {
    private final long userId;
    private final long chatroomId;
    private final String reason;

    public SubmitMessageRejectMessage(ReadBuffer stream) {
        this.userId = stream.readLong();
        this.chatroomId = stream.readLong();
        this.reason = stream.readString();
    }

    public SubmitMessageRejectMessage(long userId, long chatroomId, String reason) {
        this.userId = userId;
        this.chatroomId = chatroomId;
        this.reason = reason;
    }

    public long getUserId() {
        return userId;
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
   
        stream.writeByte(MessageTypes.SubmitMessageReject.getValue());
        stream.writeLong(getUserId());
        stream.writeLong(getChatroomId());
        stream.writeString(getReason());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=SubmitMessageReject");
        builder.append(",UserId=").append(getUserId());
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",Reason=").append(getReason());
        return builder.toString();        
    }
} 
