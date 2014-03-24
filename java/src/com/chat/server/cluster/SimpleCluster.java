package com.chat.server.cluster;

import com.chat.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 3/22/14
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleCluster implements ChatroomCluster {
    private final List<User> users;

    public SimpleCluster(int size) {
        users = new LinkedList<>();
    }

    @Override
    public void removeUser(User user) {
        users.remove(user);
    }

    public void addUser(User user) {
        users.add(user);
    }

    @Override
    public Iterator<User> getUsers() {
        return users.iterator();
    }

    @Override
    public void clear() {
        users.clear();
    }
}
