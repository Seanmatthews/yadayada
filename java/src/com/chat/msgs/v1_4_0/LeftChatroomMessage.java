package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class LeftChatroomMessage implements Message {
    private final long chatroomId;
    private final long userId;
    private final String userHandle;

    public LeftChatroomMessage(ReadBuffer stream) {
        this.chatroomId = stream.readLong();
        this.userId = stream.readLong();
        this.userHandle = stream.readString();
    }

    public LeftChatroomMessage(long chatroomId, long userId, String userHandle) {
        this.chatroomId = chatroomId;
        this.userId = userId;
        this.userHandle = userHandle;
    }

    public long getChatroomId() {
        return chatroomId;
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
   
        stream.writeByte(MessageTypes.LeftChatroom.getValue());
        stream.writeLong(getChatroomId());
        stream.writeLong(getUserId());
        stream.writeString(getUserHandle());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=LeftChatroom");
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",UserId=").append(getUserId());
        builder.append(",UserHandle=").append(getUserHandle());
        return builder.toString();        
    }
} 
