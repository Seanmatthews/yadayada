package com.chat.msgs.v1_3_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class JoinChatroomMessage implements Message {
    private final long userId;
    private final long chatroomId;
    private final long latitude;
    private final long longitude;

    public JoinChatroomMessage(ReadBuffer stream) {
        this.userId = stream.readLong();
        this.chatroomId = stream.readLong();
        this.latitude = stream.readLong();
        this.longitude = stream.readLong();
    }

    public JoinChatroomMessage(long userId, long chatroomId, long latitude, long longitude) {
        this.userId = userId;
        this.chatroomId = chatroomId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getUserId() {
        return userId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.JoinChatroom.getValue());
        stream.writeLong(getUserId());
        stream.writeLong(getChatroomId());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=JoinChatroom");
        builder.append(",UserId=").append(getUserId());
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",Latitude=").append(getLatitude());
        builder.append(",Longitude=").append(getLongitude());
        return builder.toString();        
    }
} 
