package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class RegisterMessage implements Message {
    private final String userName;
    private final String password;
    private final String handle;
    private final String UUID;
    private final long phoneNumber;

    public RegisterMessage(ReadBuffer stream) {
        this.userName = stream.readString();
        this.password = stream.readString();
        this.handle = stream.readString();
        this.UUID = stream.readString();
        this.phoneNumber = stream.readLong();
    }

    public RegisterMessage(String userName, String password, String handle, String UUID, long phoneNumber) {
        this.userName = userName;
        this.password = password;
        this.handle = handle;
        this.UUID = UUID;
        this.phoneNumber = phoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
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

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.Register.getValue());
        stream.writeString(getUserName());
        stream.writeString(getPassword());
        stream.writeString(getHandle());
        stream.writeString(getUUID());
        stream.writeLong(getPhoneNumber());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Register");
        builder.append(",UserName=").append(getUserName());
        builder.append(",Password=").append(getPassword());
        builder.append(",Handle=").append(getHandle());
        builder.append(",UUID=").append(getUUID());
        builder.append(",PhoneNumber=").append(getPhoneNumber());
        return builder.toString();        
    }
} 
