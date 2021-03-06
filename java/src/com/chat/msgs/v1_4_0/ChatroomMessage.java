package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class ChatroomMessage implements Message {
    private final long chatroomId;
    private final long chatroomOwnerId;
    private final String chatroomName;
    private final String chatroomOwnerHandle;
    private final long latitude;
    private final long longitude;
    private final long radius;
    private final int userCount;
    private final short chatActivity;
    private final byte isPrivate;

    public ChatroomMessage(ReadBuffer stream) {
        this.chatroomId = stream.readLong();
        this.chatroomOwnerId = stream.readLong();
        this.chatroomName = stream.readString();
        this.chatroomOwnerHandle = stream.readString();
        this.latitude = stream.readLong();
        this.longitude = stream.readLong();
        this.radius = stream.readLong();
        this.userCount = stream.readInt();
        this.chatActivity = stream.readShort();
        this.isPrivate = stream.readByte();
    }

    public ChatroomMessage(long chatroomId, long chatroomOwnerId, String chatroomName, String chatroomOwnerHandle, long latitude, long longitude, long radius, int userCount, short chatActivity, byte isPrivate) {
        this.chatroomId = chatroomId;
        this.chatroomOwnerId = chatroomOwnerId;
        this.chatroomName = chatroomName;
        this.chatroomOwnerHandle = chatroomOwnerHandle;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.userCount = userCount;
        this.chatActivity = chatActivity;
        this.isPrivate = isPrivate;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public long getChatroomOwnerId() {
        return chatroomOwnerId;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public String getChatroomOwnerHandle() {
        return chatroomOwnerHandle;
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

    public int getUserCount() {
        return userCount;
    }

    public short getChatActivity() {
        return chatActivity;
    }

    public byte getIsPrivate() {
        return isPrivate;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.Chatroom.getValue());
        stream.writeLong(getChatroomId());
        stream.writeLong(getChatroomOwnerId());
        stream.writeString(getChatroomName());
        stream.writeString(getChatroomOwnerHandle());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());
        stream.writeLong(getRadius());
        stream.writeInt(getUserCount());
        stream.writeShort(getChatActivity());
        stream.writeByte(getIsPrivate());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Chatroom");
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",ChatroomOwnerId=").append(getChatroomOwnerId());
        builder.append(",ChatroomName=").append(getChatroomName());
        builder.append(",ChatroomOwnerHandle=").append(getChatroomOwnerHandle());
        builder.append(",Latitude=").append(getLatitude());
        builder.append(",Longitude=").append(getLongitude());
        builder.append(",Radius=").append(getRadius());
        builder.append(",UserCount=").append(getUserCount());
        builder.append(",ChatActivity=").append(getChatActivity());
        builder.append(",IsPrivate=").append(getIsPrivate());
        return builder.toString();        
    }
} 
