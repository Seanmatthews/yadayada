package com.chat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class Chatroom {
    private final Set<User> users = new HashSet<User>();

    public String name;
    public long id;
    public User owner;

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

    public void addMessage(String message) {
        //To change body of created methods use File | Settings | File Templates.
    }
}
