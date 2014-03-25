package com.chat.msgs.v1_4_0;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

public class MessageMessage implements Message {
    private final long messageId;
    private final long messageTimestamp;
    private final long senderId;
    private final long chatroomId;
    private final String senderHandle;
    private final String message;

    public MessageMessage(ReadBuffer stream) {
        this.messageId = stream.readLong();
        this.messageTimestamp = stream.readLong();
        this.senderId = stream.readLong();
        this.chatroomId = stream.readLong();
        this.senderHandle = stream.readString();
        this.message = stream.readString();
    }

    public MessageMessage(long messageId, long messageTimestamp, long senderId, long chatroomId, String senderHandle, String message) {
        this.messageId = messageId;
        this.messageTimestamp = messageTimestamp;
        this.senderId = senderId;
        this.chatroomId = chatroomId;
        this.senderHandle = senderHandle;
        this.message = message;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getMessageTimestamp() {
        return messageTimestamp;
    }

    public long getSenderId() {
        return senderId;
    }

    public long getChatroomId() {
        return chatroomId;
    }

    public String getSenderHandle() {
        return senderHandle;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void write(ReadWriteBuffer stream) {
        int position = stream.position();
        // skip 2 bytes for length of message
        stream.advance(2);
   
        stream.writeByte(MessageTypes.Message.getValue());
        stream.writeLong(getMessageId());
        stream.writeLong(getMessageTimestamp());
        stream.writeLong(getSenderId());
        stream.writeLong(getChatroomId());
        stream.writeString(getSenderHandle());
        stream.writeString(getMessage());

        // write out length of message
        stream.writeShort(position, stream.position() - position - 2);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Msg=Message");
        builder.append(",MessageId=").append(getMessageId());
        builder.append(",MessageTimestamp=").append(getMessageTimestamp());
        builder.append(",SenderId=").append(getSenderId());
        builder.append(",ChatroomId=").append(getChatroomId());
        builder.append(",SenderHandle=").append(getSenderHandle());
        builder.append(",Message=").append(getMessage());
        return builder.toString();        
    }
} 
