package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class InviteUserMessage implements Message {
    private final long senderId;
    private final String senderHandle;
    private final long recipientId;
    private final long chatroomId;
    private final String chatroomName;
    private final long chatroomLat;
    private final long chatroomLong;
    private final long chatroomRadius;
    private final long recipientPhoneNumber;

    public InviteUserMessage(ReadBuffer stream) {
        this.senderId = stream.readLong();
        this.senderHandle = stream.readString();
        this.recipientId = stream.readLong();
        this.chatroomId = stream.readLong();
        this.chatroomName = stream.readString();
        this.chatroomLat = stream.readLong();
        this.chatroomLong = stream.readLong();
        this.chatroomRadius = stream.readLong();
        this.recipientPhoneNumber = stream.readLong();
    }

    public InviteUserMessage(long senderId, String senderHandle, long recipientId, long chatroomId, String chatroomName, long chatroomLat, long chatroomLong, long chatroomRadius, long recipientPhoneNumber) {
        this.senderId = senderId;
        this.senderHandle = senderHandle;
        this.recipientId = recipientId;
        this.chatroomId = chatroomId;
        this.chatroomName = chatroomName;
        this.chatroomLat = chatroomLat;
        this.chatroomLong = chatroomLong;
        this.chatroomRadius = chatroomRadius;
        this.recipientPhoneNumber = recipientPhoneNumber;
    }

    public long getSenderId() {
        return senderId;
    }

    public String getSenderHandle() {
        return senderHandle;
    }

    public long getRecipientId() {
        return recipientId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public String getChatroomName() {
        return chatroomName;
    }

    public long getChatroomLat() {
        return chatroomLat;
    }

    public long getChatroomLong() {
        return chatroomLong;
    }

    public long getChatroomRadius() {
        return chatroomRadius;
    }

    public long getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.InviteUser.getValue());
        stream.writeLong(getSenderId());
        stream.writeString(getSenderHandle());
        stream.writeLong(getRecipientId());
        stream.writeLong(getChatroomId());
        stream.writeString(getChatroomName());
        stream.writeLong(getChatroomLat());
        stream.writeLong(getChatroomLong());
        stream.writeLong(getChatroomRadius());
        stream.writeLong(getRecipientPhoneNumber());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=InviteUser");
        builder.append(",SenderId=").append(getSenderId());
        builder.append(",SenderHandle=").append(getSenderHandle());
        builder.append(",RecipientId=").append(getRecipientId());
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",ChatroomName=").append(getChatroomName());
        builder.append(",ChatroomLat=").append(getChatroomLat());
        builder.append(",ChatroomLong=").append(getChatroomLong());
        builder.append(",ChatroomRadius=").append(getChatroomRadius());
        builder.append(",RecipientPhoneNumber=").append(getRecipientPhoneNumber());
        return builder.toString();        
    }
} 
