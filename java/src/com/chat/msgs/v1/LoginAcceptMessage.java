package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class LoginAcceptMessage implements Message {
    private final long userId;

    public LoginAcceptMessage(ReadBuffer stream) {
        this.userId = stream.readLong();
    }

    public LoginAcceptMessage(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.LoginAccept.getValue());
        stream.writeLong(getUserId());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=LoginAccept");
        builder.append(",UserId=").append(getUserId());
        return builder.toString();        
    }
} 
