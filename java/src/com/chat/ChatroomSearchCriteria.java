package com.chat;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 8:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChatroomSearchCriteria {

    private long latitude;
    private long longitude;
    private long metersFromCoords;
    private byte onlyJoinable;

    public ChatroomSearchCriteria(long latitude, long longitude, long metersFromCoords, byte onlyJoinable) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.metersFromCoords = metersFromCoords;
        this.onlyJoinable = onlyJoinable;
    }

    public long getLatitude() { return latitude; }
    public long getLongitude() { return longitude; }
    public long getMetersFromCoords() { return metersFromCoords; }
    public boolean returnOnlyJoinable() { return onlyJoinable > 0; }
}


