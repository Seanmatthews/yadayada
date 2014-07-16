package com.chat;

import com.chat.server.cluster.ChatroomCluster;
import com.chat.server.cluster.MPSClusteringStrategy;
import com.chat.ChatroomActivity;
import com.chat.util.SerializeUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class Chatroom implements Serializable {
    private final long id;
    private final String name;
    private final User owner;
    private final long latitude;
    private final long longitude;
    private final long radius;
    private final long creationTime;
    private final ChatroomActivity chatActivity;
    private int userCount;
    private final boolean isPrivate;

    // back-reference for easy access
    private final ChatroomRepository repo;
    // TODO this should be ClusteringStrategy
    private final MPSClusteringStrategy clusterStrategy;

    public Chatroom(long id, String name, User owner, ChatroomRepository inMemoryChatroomRepository, long latitude,
                    long longitude, long radius, boolean isPrivate, ChatroomActivity chatroomActivity) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.repo = inMemoryChatroomRepository;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.clusterStrategy = new MPSClusteringStrategy(this, 0.33, 2.0);
        this.creationTime = System.currentTimeMillis() / 1000L;
        this.chatActivity = chatroomActivity;
        this.isPrivate = isPrivate;
        this.userCount = 0;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getOwner() {
        return owner;
    }

    public long getCreationTime() { return creationTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chatroom chatroom = (Chatroom) o;

        if (id != chatroom.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return name;
    }

    public ChatroomCluster addMessage(ChatMessage message) {
        chatActivity.newMessage(message);
        return clusterStrategy.addMessage(message);
    }

    public Iterator<User> getUsers() {
        return repo.getUsers(this);
    }

    public void addUser(User user) {
        this.userCount++;
        repo.addUser(this, user);
        clusterStrategy.addUser(user);
    }

    public void removeUser(User user) {
        this.userCount--;
        repo.removeUser(this, user);
        clusterStrategy.removeUser(user);
    }

    public boolean containsUser(User user) {
        return repo.containsUser(this, user);
    }

    public boolean usernameInUse(User user) {
        Iterator<User> users = repo.getUsers(this);
        while (users.hasNext()) {
            User u = users.next();
            if (u.getId() != user.getId() && u.getHandle().equals(user.getHandle())) {
                return true;
            }
        }
        return false;
    }

    public boolean usernameInUse(String username) {
        Iterator<User> users = repo.getUsers(this);
        while (users.hasNext()) {
            User u = users.next();
            if (u.getHandle().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public int getUserCount() {
        return this.userCount;
//        return repo.getChatroomUserCount(this);
    }

    public long getRadius() {
        return radius;
    }

    public long getLongitude() {
        return longitude;
    }

    public long getLatitude() {
        return latitude;
    }

    public short getChatActivity() { return chatActivity.getActivityPercentage(); }

    public boolean global() { return 0 >= radius; }

    public boolean isPrivate() { return isPrivate; }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // perform the default serialization for all non-transient, non-static fields
        out.defaultWriteObject();

        out.writeLong(id);
        out.writeObject(name);
        out.writeObject(owner);
        out.writeLong(latitude);
        out.writeLong(longitude);
        out.writeLong(radius);
        out.writeLong(creationTime);
        out.writeObject(chatActivity);
        out.writeShort(userCount);
        out.writeBoolean(isPrivate);

        // Cyclic references are handled by Java Serialization
        out.writeObject(repo);
        out.writeObject(clusterStrategy);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // always perform the default de-serialization first
        in.defaultReadObject();

        SerializeUtil.setPrivateFinalVar(this, "id", new Long(in.readLong()));
        SerializeUtil.setPrivateFinalVar(this, "name", new String((String)in.readObject()));
        SerializeUtil.setPrivateFinalVar(this, "owner", (User)in.readObject());
        SerializeUtil.setPrivateFinalVar(this, "latitude", new Long(in.readLong()));
        SerializeUtil.setPrivateFinalVar(this, "longitude", new Long(in.readLong()));
        SerializeUtil.setPrivateFinalVar(this, "radius", new Long(in.readLong()));
        SerializeUtil.setPrivateFinalVar(this, "creationTime", new Long(in.readLong()));
        SerializeUtil.setPrivateFinalVar(this, "chatActivity", (ChatroomActivity)in.readObject());
        userCount = new Short(in.readShort());
        SerializeUtil.setPrivateFinalVar(this, "isPrivate", new Boolean(in.readBoolean()));
        SerializeUtil.setPrivateFinalVar(this, "repo", (ChatroomRepository)in.readObject());
        SerializeUtil.setPrivateFinalVar(this, "clusterStrategy", (MPSClusteringStrategy)clusterStrategy);
    }
}
