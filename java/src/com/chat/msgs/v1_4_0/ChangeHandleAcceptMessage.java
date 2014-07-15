package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class ChangeHandleAcceptMessage implements Message {
    private final String handle;

    public ChangeHandleAcceptMessage(ReadBuffer stream) {
        this.handle = stream.readString();
    }

    public ChangeHandleAcceptMessage(String handle) {
        this.handle = handle;
    }

    public String getHandle() {
        return handle;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.ChangeHandleAccept.getValue());
        stream.writeString(getHandle());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=ChangeHandleAccept");
        builder.append(",Handle=").append(getHandle());
        return builder.toString();        
    }
} 
