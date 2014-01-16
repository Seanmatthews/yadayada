package com.chat;

import com.chat.server.cluster.ChatroomCluster;
import com.chat.server.cluster.MPSClusteringStrategy;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class Chatroom {
    private final long id;
    private final String name;
    private final User owner;
    private final long latitude;
    private final long longitude;
    private final long radius;
    private final long creationTime;

    // back-reference for easy access
    private final ChatroomRepository repo;
    private final MPSClusteringStrategy clusterStrategy;

    public Chatroom(long id, String name, User owner, ChatroomRepository inMemoryChatroomRepository, long latitude, long longitude, long radius) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.repo = inMemoryChatroomRepository;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.clusterStrategy = new MPSClusteringStrategy(this, 0.33, 2.0);
        this.creationTime = System.currentTimeMillis() / 1000L;
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
        return clusterStrategy.addMessage(message);
    }

    public Iterator<User> getUsers() {
        return repo.getUsers(this);
    }

    public void addUser(User user) {
        repo.addUser(this, user);
        clusterStrategy.addUser(user);
    }

    public void removeUser(User user) {
        repo.removeUser(this, user);
        clusterStrategy.removeUser(user);
    }

    public boolean containsUser(User user) {
        return repo.containsUser(this, user);
    }

    public int getUserCount() {
        return repo.getChatroomUserCount(this);
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

    public boolean global() { return 0 >= radius; }
}
