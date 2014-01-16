package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class JoinedChatroomMessage implements Message {
    private final long chatroomId;
    private final String chatroomName;
    private final long userId;
    private final String userHandle;

    public JoinedChatroomMessage(ReadBuffer stream) {
        this.chatroomId = stream.readLong();
        this.chatroomName = stream.readString();
        this.userId = stream.readLong();
        this.userHandle = stream.readString();
    }

    public JoinedChatroomMessage(long chatroomId, String chatroomName, long userId, String userHandle) {
        this.chatroomId = chatroomId;
        this.chatroomName = chatroomName;
        this.userId = userId;
        this.userHandle = userHandle;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserHandle() {
        return userHandle;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.JoinedChatroom.getValue());
        stream.writeLong(getChatroomId());
        stream.writeString(getChatroomName());
        stream.writeLong(getUserId());
        stream.writeString(getUserHandle());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=JoinedChatroom");
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",ChatroomName=").append(getChatroomName());
        builder.append(",UserId=").append(getUserId());
        builder.append(",UserHandle=").append(getUserHandle());
        return builder.toString();        
    }
} 
