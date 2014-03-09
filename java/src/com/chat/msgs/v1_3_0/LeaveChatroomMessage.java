package com.chat.msgs.v1_3_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class LeaveChatroomMessage implements Message {
    private final long userId;
    private final long chatroomId;

    public LeaveChatroomMessage(ReadBuffer stream) {
        this.userId = stream.readLong();
        this.chatroomId = stream.readLong();
    }

    public LeaveChatroomMessage(long userId, long chatroomId) {
        this.userId = userId;
        this.chatroomId = chatroomId;
    }

    public long getUserId() {
        return userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.LeaveChatroom.getValue());
        stream.writeLong(getUserId());
        stream.writeLong(getChatroomId());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=LeaveChatroom");
        builder.append(",UserId=").append(getUserId());
        builder.append(",ChatroomId=").append(getChatroomId());
        return builder.toString();        
    }
} 
