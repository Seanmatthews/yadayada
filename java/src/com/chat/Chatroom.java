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
    //private static final int MESSAGES_TO_STORE = 20;
    //private final Queue<ChatMessage> recentMessages = new ConcurrentLinkedQueue<>();
    //private final AtomicInteger messageCount = new AtomicInteger(0);

    private final long id;
    private final String name;
    private final User owner;

    // back-reference for easy access
    private final ChatroomRepository repo;

    public Chatroom(long id, String name, User owner, ChatroomRepository inMemoryChatroomRepository) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.repo = inMemoryChatroomRepository;
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

    /*public void addMessage(ChatMessage message) {
        int count = messageCount.incrementAndGet();

        // don't want to get recentMessages.size() because it iterates through the entire list
        if (count > MESSAGES_TO_STORE) {
            recentMessages.remove();
        }

        recentMessages.add(message);
    }

    public Iterator<ChatMessage> getRecentMessages() {
        return recentMessages.iterator();
    }   */

    public Iterator<User> getUsers() {
        return repo.getUsers(this);
    }

    public void addUser(User user) {
        repo.addUser(this, user);
    }

    public void removeUser(User user) {
        repo.removeUser(this, user);
    }

    public boolean containsUser(User user) {
        return repo.containsUser(this, user);
    }
}
