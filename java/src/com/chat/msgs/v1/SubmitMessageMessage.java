package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class SubmitMessageMessage implements Message {
    private final long userId;
    private final long chatroomId;
    private final String message;

    public SubmitMessageMessage(ReadBuffer stream) {
        this.userId = stream.readLong();
        this.chatroomId = stream.readLong();
        this.message = stream.readString();
    }

    public SubmitMessageMessage(long userId, long chatroomId, String message) {
        this.userId = userId;
        this.chatroomId = chatroomId;
        this.message = message;
    }

    public long getUserId() {
        return userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        stream.advance(2);
   
        stream.writeByte(MessageTypes.SubmitMessage.getValue());
        stream.writeLong(getUserId());
        stream.writeLong(getChatroomId());
        stream.writeString(getMessage());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }
} 
