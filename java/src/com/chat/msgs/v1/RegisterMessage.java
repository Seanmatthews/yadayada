package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class RegisterMessage implements Message {
    private final String userName;
    private final String password;
    private final String handle;
    private final String UUID;

    public RegisterMessage(ReadBuffer stream) {
        this.userName = stream.readString();
        this.password = stream.readString();
        this.handle = stream.readString();
        this.UUID = stream.readString();
    }

    public RegisterMessage(String userName, String password, String handle, String UUID) {
        this.userName = userName;
        this.password = password;
        this.handle = handle;
        this.UUID = UUID;
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
        return builder.toString();        
    }
} 
