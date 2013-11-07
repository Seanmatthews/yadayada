package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class RegisterAcceptMessage implements Message {
    private final long userId;

    public RegisterAcceptMessage(ReadBuffer stream) {
        this.userId = stream.readLong();
    }

    public RegisterAcceptMessage(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        stream.advance(2);
   
        stream.writeByte(MessageTypes.RegisterAccept.getValue());
        stream.writeLong(getUserId());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }
} 
