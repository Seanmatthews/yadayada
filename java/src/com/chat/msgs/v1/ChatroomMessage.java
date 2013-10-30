package com.chat.msgs.v1;

public class ChatroomMessage {
    private final long chatroomId;
    private final long chatroomOwnerId;
    private final String chatroomName;
    private final String chatroomOwnerHandle;
    private final long latitude;
    private final long longitude;
    private final long radius;

    public ChatroomMessage(long chatroomId, long chatroomOwnerId, String chatroomName, String chatroomOwnerHandle, long latitude, long longitude, long radius) {
        this.chatroomId = chatroomId;
        this.chatroomOwnerId = chatroomOwnerId;
        this.chatroomName = chatroomName;
        this.chatroomOwnerHandle = chatroomOwnerHandle;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
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
} 
