package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class JoinedChatroomMessage implements Message {
    private final long userId;
    private final String userHandle;
    private final long chatroomId;
    private final long chatroomOwnerId;
    private final String chatroomName;
    private final String chatroomOwnerHandle;
    private final long latitude;
    private final long longitude;
    private final long radius;
    private final int userCount;
    private final short chatActivity;

    public JoinedChatroomMessage(ReadBuffer stream) {
        this.userId = stream.readLong();
        this.userHandle = stream.readString();
        this.chatroomId = stream.readLong();
        this.chatroomOwnerId = stream.readLong();
        this.chatroomName = stream.readString();
        this.chatroomOwnerHandle = stream.readString();
        this.latitude = stream.readLong();
        this.longitude = stream.readLong();
        this.radius = stream.readLong();
        this.userCount = stream.readInt();
        this.chatActivity = stream.readShort();
    }

    public JoinedChatroomMessage(long userId, String userHandle, long chatroomId, long chatroomOwnerId, String chatroomName, String chatroomOwnerHandle, long latitude, long longitude, long radius, int userCount, short chatActivity) {
        this.userId = userId;
        this.userHandle = userHandle;
        this.chatroomId = chatroomId;
        this.chatroomOwnerId = chatroomOwnerId;
        this.chatroomName = chatroomName;
        this.chatroomOwnerHandle = chatroomOwnerHandle;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.userCount = userCount;
        this.chatActivity = chatActivity;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserHandle() {
        return userHandle;
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

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.JoinedChatroom.getValue());
        stream.writeLong(getUserId());
        stream.writeString(getUserHandle());
        stream.writeLong(getChatroomId());
        stream.writeLong(getChatroomOwnerId());
        stream.writeString(getChatroomName());
        stream.writeString(getChatroomOwnerHandle());
        stream.writeLong(getLatitude());
        stream.writeLong(getLongitude());
        stream.writeLong(getRadius());
        stream.writeInt(getUserCount());
        stream.writeShort(getChatActivity());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=JoinedChatroom");
        builder.append(",UserId=").append(getUserId());
        builder.append(",UserHandle=").append(getUserHandle());
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",ChatroomOwnerId=").append(getChatroomOwnerId());
        builder.append(",ChatroomName=").append(getChatroomName());
        builder.append(",ChatroomOwnerHandle=").append(getChatroomOwnerHandle());
        builder.append(",Latitude=").append(getLatitude());
        builder.append(",Longitude=").append(getLongitude());
        builder.append(",Radius=").append(getRadius());
        builder.append(",UserCount=").append(getUserCount());
        builder.append(",ChatActivity=").append(getChatActivity());
        return builder.toString();        
    }
} 
