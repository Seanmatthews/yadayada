package com.chat.msgs.v1;

public class CreateChatroomMessage {
    private final long ownerId;
    private final String chatroomName;
    private final long latitude;
    private final long longitude;
    private final long radius;

    public CreateChatroomMessage(long ownerId, String chatroomName, long latitude, long longitude, long radius) {
        this.ownerId = ownerId;
        this.chatroomName = chatroomName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public String getChatroomName() {
        return chatroomName;
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
