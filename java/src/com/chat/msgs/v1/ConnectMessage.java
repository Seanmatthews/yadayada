package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class ConnectMessage implements Message {
    private final int APIVersion;
    private final String UUID;

    public ConnectMessage(ReadBuffer stream) {
        this.APIVersion = stream.readInt();
        this.UUID = stream.readString();
    }

    public ConnectMessage(int APIVersion, String UUID) {
        this.APIVersion = APIVersion;
        this.UUID = UUID;
    }

    public int getAPIVersion() {
        return APIVersion;
    }

    public String getUUID() {
        return UUID;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        stream.advance(2);
   
        stream.writeByte(MessageTypes.Connect.getValue());
        stream.writeInt(getAPIVersion());
        stream.writeString(getUUID());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }
} 
