package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class LeftChatroomMessage implements Message {
    private final long chatroomId;
    private final long userId;

    public LeftChatroomMessage(ReadBuffer stream) {
        this.chatroomId = stream.readLong();
        this.userId = stream.readLong();
    }

    public LeftChatroomMessage(long chatroomId, long userId) {
        this.chatroomId = chatroomId;
        this.userId = userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.LeftChatroom.getValue());
        stream.writeLong(getChatroomId());
        stream.writeLong(getUserId());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=LeftChatroom");
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",UserId=").append(getUserId());
        return builder.toString();        
    }
} 
