package com.chat.msgs.v1;

public class JoinedChatroomMessage {
    private final long chatroomId;
    private final long userId;
    private final String userHandle;

    public JoinedChatroomMessage(long chatroomId, long userId, String userHandle) {
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
} 
