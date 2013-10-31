package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class LoginMessage implements Message {
    private final String userName;
    private final String password;

    public LoginMessage(BinaryStream stream) throws IOException {
        this.userName = stream.readString();
        this.password = stream.readString();
    }

    public LoginMessage(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           LoginMessage msg = this;
           stream.startWriting(1 + getStrLen(msg.getUserName()) + getStrLen(msg.getPassword()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.Login.getValue());
        stream.writeString(getUserName());
        stream.writeString(getPassword());
        stream.finishWriting();
    }
} 
