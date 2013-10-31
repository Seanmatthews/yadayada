package com.chat;

import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class Chatroom {
    private static final int MESSAGES_TO_STORE = 20;

    private final Set<User> users = Collections.newSetFromMap(new ConcurrentHashMap<User, Boolean>());
    private final Queue<ChatMessage> recentMessages = new ConcurrentLinkedQueue<>();
    private final AtomicInteger messageCount = new AtomicInteger(0);

    private final long id;
    private final String name;
    private final User owner;

    public Chatroom(long id, String name, User owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
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

    public void addUser(User user) {
        users.add(user);
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public Iterator<User> getUsers() {
        return users.iterator();
    }

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

    public void addMessage(ChatMessage message) {
        int count = messageCount.incrementAndGet();

        // don't want to get recentMessages.size() because it iterates through the entire list
        if (count > MESSAGES_TO_STORE) {
            recentMessages.remove();
        }

        recentMessages.add(message);
    }

    public Iterator<ChatMessage> getRecentMessages() {
        return recentMessages.iterator();
    }

    public boolean containsUser(User sender) {
        return users.contains(sender);
    }
}
