package com.chat.impl;

import com.chat.Chatroom;
import com.chat.ChatroomRepository;
import com.chat.ChatroomSearchCriteria;
import com.chat.User;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 8:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class InMemoryChatroomRepository implements ChatroomRepository {

    // We have lots of threads accessing this repository
    // We need to keep nextChatroomId and the map in sync
    private final AtomicLong nextChatroomId = new AtomicLong(1);
    private final Map<Long, Chatroom> chatroomIdMap = new ConcurrentHashMap<>();

    private final Map<Chatroom, Set<User>> chatroomUserMap = new ConcurrentHashMap<>();

    @Override
    public Chatroom createChatroom(User owner, String name) {
        Chatroom chatroom = new Chatroom(nextChatroomId.getAndIncrement(), name, owner, this);
        addChatroom(chatroom);
        return chatroom;
    }

    @Override
    public Iterator<Chatroom> search(ChatroomSearchCriteria search) {
        return chatroomIdMap.values().iterator();
    }

    @Override
    public Chatroom get(long chatroomId) {
        return chatroomIdMap.get(chatroomId);
    }

    @Override
    public void addChatroom(Chatroom chatroom) {
        chatroomIdMap.put(chatroom.getId(), chatroom);
        chatroomUserMap.put(chatroom, Collections.newSetFromMap(new ConcurrentHashMap<User, Boolean>()));
    }

    @Override
    public Iterator<Chatroom> iterator() {
        return chatroomIdMap.values().iterator();
    }

    @Override
    public Iterator<User> getUsers(Chatroom chatroom) {
        return chatroomUserMap.get(chatroom).iterator();
    }

    @Override
    public void addUser(Chatroom chatroom, User user) {
        chatroomUserMap.get(chatroom).add(user);
    }

    @Override
    public void removeUser(Chatroom chatroom, User user) {
        chatroomUserMap.get(chatroom).remove(user);
    }

    @Override
    public boolean containsUser(Chatroom chatroom, User sender) {
        return chatroomUserMap.get(chatroom).contains(sender);
    }
}
