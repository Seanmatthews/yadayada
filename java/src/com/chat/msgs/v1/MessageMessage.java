package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.Message;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

public class MessageMessage implements Message {
    private final long messageId;
    private final long messageTimestamp;
    private final long senderId;
    private final long chatroomId;
    private final String senderHandle;
    private final String message;

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
    public void write(BinaryStream stream) throws IOException {
        // backwards compatability
        if (stream.isStream()) {
           MessageMessage msg = this;
           stream.startWriting(1 + 8 + 8 + 8 + 8 + getStrLen(msg.getSenderHandle()) + getStrLen(msg.getMessage()));
        }  
        else {
           stream.startWriting();
        }

        stream.writeByte(MessageTypes.Message.getValue());
        stream.writeLong(getMessageId());
        stream.writeLong(getMessageTimestamp());
        stream.writeLong(getSenderId());
        stream.writeLong(getChatroomId());
        stream.writeString(getSenderHandle());
        stream.writeString(getMessage());
        stream.finishWriting();
    }
} 
