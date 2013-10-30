package com.chat.msgs.v1;

public class SearchChatroomsMessage {
    private final long latitude;
    private final long longitude;

    public SearchChatroomsMessage(long latitude, long longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getLatitude() {
        return latitude;
    }

    public long getLongitude() {
        return longitude;
    }
} 
