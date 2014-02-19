package com.chat.msgs.v1;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class InviteUserMessage implements Message {
    private final long senderId;
    private final long recipientId;
    private final long chatroomId;
    private final long recipientPhoneNumber;

    public InviteUserMessage(ReadBuffer stream) {
        this.senderId = stream.readLong();
        this.recipientId = stream.readLong();
        this.chatroomId = stream.readLong();
        this.recipientPhoneNumber = stream.readLong();
    }

    public InviteUserMessage(long senderId, long recipientId, long chatroomId, long recipientPhoneNumber) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.chatroomId = chatroomId;
        this.recipientPhoneNumber = recipientPhoneNumber;
    }

    public long getSenderId() {
        return senderId;
    }

    public long getRecipientId() {
        return recipientId;
    }

    public long getChatroomId() {
        return chatroomId;
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
        stream.writeLong(getRecipientId());
        stream.writeLong(getChatroomId());
        stream.writeLong(getRecipientPhoneNumber());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=InviteUser");
        builder.append(",SenderId=").append(getSenderId());
        builder.append(",RecipientId=").append(getRecipientId());
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",RecipientPhoneNumber=").append(getRecipientPhoneNumber());
        return builder.toString();        
    }
} 
