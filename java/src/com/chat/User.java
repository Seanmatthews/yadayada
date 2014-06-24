package com.chat;

import com.chat.server.cluster.MPSClusteringStrategy;
import com.chat.util.SerializeUtil;
import com.relayrides.pushy.apns.util.TokenUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 9:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class User implements Serializable {
    private final long id;
    private final String uuid;
    private String login;
    private final String password;
    private String handle;
    private final long phoneNumber;
    private final UserRepository repo;
    private double latitude;
    private double longitude;
    private long lastHeartbeat;
    private final String deviceToken;
    private boolean connected;

    public User(long id, String uuid, String handle, long phoneNumber, String deviceTokenString, UserRepository repo) {
        this(id, uuid, "QR", "QR", handle, phoneNumber, deviceTokenString, repo);
    }

    public User(long id, String uuid, String login, String password, String handle, long phoneNumber, String deviceTokenString,
                UserRepository repo) {
        this.id = id;
        this.uuid = uuid;
        this.login = login;
        this.password = password;
        this.handle = handle;
        this.phoneNumber = phoneNumber;
        this.repo = repo;
        this.latitude = 0;
        this.longitude = 0;
        this.lastHeartbeat = Calendar.getInstance().getTimeInMillis() / 1000L;
        this.deviceToken = deviceTokenString;
        this.connected = true;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
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

    public void setHandle(String handle) { this.handle = handle; }

    public void setLogin(String login) { this.login = login; }

    public String getDeviceToken() { return deviceToken; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public long getLastHeartbeat() { return lastHeartbeat; }

    public long getId() {
        return id;
    }

    public String getUUID() { return uuid; }

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

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // perform the default serialization for all non-transient, non-static fields
        out.defaultWriteObject();

        out.writeLong(id);
        out.writeObject(uuid);
        out.writeObject(login);
        out.writeObject(password);
        out.writeObject(handle);
        out.writeLong(phoneNumber);
        // Cyclic references are handled by Java Serialization
        out.writeObject(repo);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeLong(lastHeartbeat);
        out.writeObject(deviceToken);
        out.writeBoolean(connected);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // always perform the default de-serialization first
        in.defaultReadObject();

        SerializeUtil.setPrivateFinalVar(this, "id", new Long(in.readLong()));
        SerializeUtil.setPrivateFinalVar(this, "uuid", new String((String)in.readObject()));
        login = new String((String)in.readObject());
        SerializeUtil.setPrivateFinalVar(this, "password", new String((String)in.readObject()));
        handle = new String((String)in.readObject());
        SerializeUtil.setPrivateFinalVar(this, "phoneNumber", new Long(in.readLong()));
        SerializeUtil.setPrivateFinalVar(this, "repo", (UserRepository)in.readObject());
        latitude = new Double(in.readDouble());
        longitude = new Double(in.readDouble());
        lastHeartbeat = new Long(in.readLong());
        SerializeUtil.setPrivateFinalVar(this, "deviceToken", new String((String)in.readObject()));
        connected = new Boolean(in.readBoolean());
    }
}
