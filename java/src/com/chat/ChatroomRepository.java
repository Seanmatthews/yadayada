package com.chat;

import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 8:47 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatroomRepository {
    Chatroom createChatroom(User owner, String name);
    Iterator<Chatroom> search(ChatroomSearchCriteria search);
    Chatroom get(long chatroomId);
    void addChatroom(Chatroom chatroom);
    Iterator<Chatroom> iterator();

    Iterator<User> getUsers(Chatroom chatroom);
    void addUser(Chatroom chatroom, User user);
    void removeUser(Chatroom chatroom, User user);
    boolean containsUser(Chatroom chatroom, User sender);
}