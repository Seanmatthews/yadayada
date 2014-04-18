package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class ConnectMessage implements Message {
    private final int APIVersion;
    private final String UUID;
    private final String deviceToken;

    public ConnectMessage(ReadBuffer stream) {
        this.APIVersion = stream.readInt();
        this.UUID = stream.readString();
        this.deviceToken = stream.readString();
    }

    public ConnectMessage(int APIVersion, String UUID, String deviceToken) {
        this.APIVersion = APIVersion;
        this.UUID = UUID;
        this.deviceToken = deviceToken;
    }

    public int getAPIVersion() {
        return APIVersion;
    }

    public String getUUID() {
        return UUID;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.Connect.getValue());
        stream.writeInt(getAPIVersion());
        stream.writeString(getUUID());
        stream.writeString(getDeviceToken());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Connect");
        builder.append(",APIVersion=").append(getAPIVersion());
        builder.append(",UUID=").append(getUUID());
        builder.append(",DeviceToken=").append(getDeviceToken());
        return builder.toString();        
    }
} 
