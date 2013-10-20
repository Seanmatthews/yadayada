package com.chat.server.impl;

import com.chat.Chatroom;
import com.chat.ChatroomRepository;
import com.chat.ChatroomSearchCriteria;
import com.chat.User;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 8:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class InMemoryChatroomRepository implements ChatroomRepository {
    private final Map<Long, Chatroom> chatroomIdMap = new HashMap<>();
    private final List<Chatroom> allChatrooms = new ArrayList<>();

    private int nextChatroomId = 1;

    @Override
    public Chatroom createChatroom(User owner, String name) {
        Chatroom chatroom = new Chatroom();
        chatroom.id = nextChatroomId++;
        chatroom.name = name;
        chatroom.owner = owner;
        chatroom.addUser(owner);

        chatroomIdMap.put(chatroom.id, chatroom);
        allChatrooms.add(chatroom);
        return chatroom;
    }

    @Override
    public Iterator<Chatroom> search(ChatroomSearchCriteria search) {
        return allChatrooms.iterator();
    }

    @Override
    public Chatroom get(long chatroomId) {
        return chatroomIdMap.get(chatroomId);
    }

    @Override
    public void addChatroom(Chatroom chatroom) {
        chatroomIdMap.put(chatroom.id, chatroom);
        allChatrooms.add(chatroom);
    }
}
