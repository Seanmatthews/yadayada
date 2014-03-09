package com.chat.msgs.v1_3_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class LoginMessage implements Message {
    private final String userName;
    private final String password;

    public LoginMessage(ReadBuffer stream) {
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
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.Login.getValue());
        stream.writeString(getUserName());
        stream.writeString(getPassword());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Login");
        builder.append(",UserName=").append(getUserName());
        builder.append(",Password=").append(getPassword());
        return builder.toString();        
    }
} 
