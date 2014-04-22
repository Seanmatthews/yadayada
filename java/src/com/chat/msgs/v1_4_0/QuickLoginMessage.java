package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class QuickLoginMessage implements Message {
    private final String handle;
    private final String UUID;
    private final long phoneNumber;
    private final String deviceToken;

    public QuickLoginMessage(ReadBuffer stream) {
        this.handle = stream.readString();
        this.UUID = stream.readString();
        this.phoneNumber = stream.readLong();
        this.deviceToken = stream.readString();
    }

    public QuickLoginMessage(String handle, String UUID, long phoneNumber, String deviceToken) {
        this.handle = handle;
        this.UUID = UUID;
        this.phoneNumber = phoneNumber;
        this.deviceToken = deviceToken;
    }

    public String getHandle() {
        return handle;
    }

    public String getUUID() {
        return UUID;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.QuickLogin.getValue());
        stream.writeString(getHandle());
        stream.writeString(getUUID());
        stream.writeLong(getPhoneNumber());
        stream.writeString(getDeviceToken());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=QuickLogin");
        builder.append(",Handle=").append(getHandle());
        builder.append(",UUID=").append(getUUID());
        builder.append(",PhoneNumber=").append(getPhoneNumber());
        builder.append(",DeviceToken=").append(getDeviceToken());
        return builder.toString();        
    }
} 
