package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class CreateChatroomMessage implements Message {
    private final long ownerId;
    private final String chatroomName;
    private final long latitude;
    private final long longitude;
    private final long radius;

    public CreateChatroomMessage(ReadBuffer stream) {
        this.ownerId = stream.readLong();
        this.chatroomName = stream.readString();
        this.latitude = stream.readLong();
        this.longitude = stream.readLong();
        this.radius = stream.readLong();
    }

    public CreateChatroomMessage(long ownerId, String chatroomName, long latitude, long longitude, long radius) {
        this.ownerId = ownerId;
        this.chatroomName = chatroomName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public long getRadius() {
        return radius;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.CreateChatroom.getValue());
        stream.writeLong(getOwnerId());
        stream.writeString(getChatroomName());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());
        stream.writeLong(getRadius());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=CreateChatroom");
        builder.append(",OwnerId=").append(getOwnerId());
        builder.append(",ChatroomName=").append(getChatroomName());
        builder.append(",Latitude=").append(getLatitude());
        builder.append(",Longitude=").append(getLongitude());
        builder.append(",Radius=").append(getRadius());
        return builder.toString();        
    }
} 
