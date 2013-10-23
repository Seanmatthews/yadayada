package com.chat.msgs.v1;

public class JoinChatroomMessage {
    private final long userId;
    private final long chatroomId;
    private final long latitude;
    private final long longitude;

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
} 
