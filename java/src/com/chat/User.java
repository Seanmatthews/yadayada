package com.chat;

import com.relayrides.pushy.apns.util.TokenUtil;

import java.util.Calendar;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 9:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class User {
    private final long id;
    private final String login;
    private final String password;
    private final String handle;
    private final long phoneNumber;
    private final UserRepository repo;
    private double latitude;
    private double longitude;
    private long lastHeartbeat;
    private final String deviceToken;

    public User(long id, String handle, long phoneNumber, String deviceTokenString, UserRepository repo) {
        this(id, "QR", "QR", handle, phoneNumber, deviceTokenString, repo);
    }

    public User(long id, String login, String password, String handle, long phoneNumber, String deviceTokenString,
                UserRepository repo) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.handle = handle;
        this.phoneNumber = phoneNumber;
        this.repo = repo;
        this.latitude = 0;
        this.longitude = 0;
        this.lastHeartbeat = Calendar.getInstance().getTimeInMillis() / 1000L;
        this.deviceToken = deviceTokenString;
    }

    public void setLatitude(long latitude) {
        // convert from message latitude
        this.latitude = latitude / 1000000. - 400;
    }

    public void setLongitude(long longitude) {
        // convert from message longitude
        this.longitude = longitude / 1000000. - 400;
    }

    public void setLastHeartbeat(long timestamp) {
        this.lastHeartbeat = timestamp;
    }

    public String getDeviceToken() { return deviceToken; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public long getLastHeartbeat() { return lastHeartbeat; }

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getHandle() {
        return handle;
    }

    public Long getPhoneNumber() { return phoneNumber; }

    public void addToChatroom(Chatroom chatroom) {
        repo.addToChatroom(this, chatroom);
    }

    public void removeFromChatroom(Chatroom chatroom) {
        repo.removeFromChatroom(this, chatroom);
    }

    public Iterator<Chatroom> getChatrooms() {
        return repo.getChatrooms(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return login + "<" + handle + ">";
    }
}
