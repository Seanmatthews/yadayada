package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class ChangeHandleMessage implements Message {
    private final long userId;
    private final String oldHandle;
    private final String handle;

    public ChangeHandleMessage(ReadBuffer stream) {
        this.userId = stream.readLong();
        this.oldHandle = stream.readString();
        this.handle = stream.readString();
    }

    public ChangeHandleMessage(long userId, String oldHandle, String handle) {
        this.userId = userId;
        this.oldHandle = oldHandle;
        this.handle = handle;
    }

    public long getUserId() {
        return userId;
    }

    public String getOldHandle() {
        return oldHandle;
    }

    public String getHandle() {
        return handle;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.ChangeHandle.getValue());
        stream.writeLong(getUserId());
        stream.writeString(getOldHandle());
        stream.writeString(getHandle());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=ChangeHandle");
        builder.append(",UserId=").append(getUserId());
        builder.append(",OldHandle=").append(getOldHandle());
        builder.append(",Handle=").append(getHandle());
        return builder.toString();        
    }
} 
