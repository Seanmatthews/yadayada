package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class RegisterMessage implements Message {
    private final String userName;
    private final String password;
    private final String handle;

    public RegisterMessage(BinaryStream stream) throws IOException {
        this.userName = stream.readString();
        this.password = stream.readString();
        this.handle = stream.readString();
    }

    public RegisterMessage(String userName, String password, String handle) {
        this.userName = userName;
        this.password = password;
        this.handle = handle;
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

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           RegisterMessage msg = this;
           stream.startWriting(1 + getStrLen(msg.getUserName()) + getStrLen(msg.getPassword()) + getStrLen(msg.getHandle()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.Register.getValue());
        stream.writeString(getUserName());
        stream.writeString(getPassword());
        stream.writeString(getHandle());
        stream.finishWriting();
    }
} 
