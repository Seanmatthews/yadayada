package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class StreamResetMessage implements Message {
    private final long userId;
    private final byte appAwake;

    public StreamResetMessage(ReadBuffer stream) {
        this.userId = stream.readLong();
        this.appAwake = stream.readByte();
    }

    public StreamResetMessage(long userId, byte appAwake) {
        this.userId = userId;
        this.appAwake = appAwake;
    }

    public long getUserId() {
        return userId;
    }

    public byte getAppAwake() {
        return appAwake;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.StreamReset.getValue());
        stream.writeLong(getUserId());
        stream.writeByte(getAppAwake());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=StreamReset");
        builder.append(",UserId=").append(getUserId());
        builder.append(",AppAwake=").append(getAppAwake());
        return builder.toString();        
    }
} 
