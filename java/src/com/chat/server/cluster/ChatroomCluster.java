package com.chat.server.cluster;

import com.chat.User;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/19/13
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatroomCluster {
    void removeUser(User user);
    void addUser(User user);
    Iterator<User> getUsers();
    void clear();
}
