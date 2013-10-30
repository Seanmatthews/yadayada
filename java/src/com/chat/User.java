package com.chat;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Set<Chatroom> chatrooms = Collections.newSetFromMap(new ConcurrentHashMap<Chatroom, Boolean>());

    public User(long id, String handle) {
        this.id = id;
        this.handle = handle;
        this.login = "QR";
        this.password = "QR";
    }

    public User(long id, String login, String password, String handle) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.handle = handle;
    }

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

    public void addToChatroom(Chatroom chatroom) {
        chatrooms.add(chatroom);
    }

    public void removeFromChatroom(Chatroom chatroom) {
        chatrooms.remove(chatroom);
    }

    public Iterator<Chatroom> getChatrooms() {
        return chatrooms.iterator();
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
