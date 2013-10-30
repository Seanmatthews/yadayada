package com.chat.msgs.v1;

public class MessageMessage {
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
} 
