package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class ConnectMessage implements Message {
    private final int APIVersion;
    private final String UUID;

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
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           ConnectMessage msg = this;
           stream.startWriting(1 + 4 + getStrLen(msg.getUUID()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.Connect.getValue());
        stream.writeInt(getAPIVersion());
        stream.writeString(getUUID());
        stream.finishWriting();
    }
} 
