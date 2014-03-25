package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class StreamResetMessage implements Message {
    private final long userId;

    public StreamResetMessage(ReadBuffer stream) {
        this.userId = stream.readLong();
    }

    public StreamResetMessage(long userId) {
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
   
        stream.writeByte(MessageTypes.StreamReset.getValue());
        stream.writeLong(getUserId());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=StreamReset");
        builder.append(",UserId=").append(getUserId());
        return builder.toString();        
    }
} 
