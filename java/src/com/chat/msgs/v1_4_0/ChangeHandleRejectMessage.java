package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class ChangeHandleRejectMessage implements Message {
    private final String handle;
    private final String oldHandle;
    private final String reason;

    public ChangeHandleRejectMessage(ReadBuffer stream) {
        this.handle = stream.readString();
        this.oldHandle = stream.readString();
        this.reason = stream.readString();
    }

    public ChangeHandleRejectMessage(String handle, String oldHandle, String reason) {
        this.handle = handle;
        this.oldHandle = oldHandle;
        this.reason = reason;
    }

    public String getHandle() {
        return handle;
    }

    public String getOldHandle() {
        return oldHandle;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.ChangeHandleReject.getValue());
        stream.writeString(getHandle());
        stream.writeString(getOldHandle());
        stream.writeString(getReason());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=ChangeHandleReject");
        builder.append(",Handle=").append(getHandle());
        builder.append(",OldHandle=").append(getOldHandle());
        builder.append(",Reason=").append(getReason());
        return builder.toString();        
    }
} 
