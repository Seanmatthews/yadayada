package com.chat.impl;

import com.chat.Chatroom;
import com.chat.ChatroomRepository;
import com.chat.ChatroomSearchCriteria;
import com.chat.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    private volatile long nextChatroomId = 1;
    private final Map<Long, Chatroom> chatroomIdMap = new ConcurrentHashMap<>();

    @Override
    public Chatroom createChatroom(User owner, String name) {
        Chatroom chatroom = new Chatroom(nextChatroomId++, name, owner);
        chatroom.addUser(owner);

        chatroomIdMap.put(chatroom.getId(), chatroom);
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
    }
}
