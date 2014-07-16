package com.chat.impl;

import com.chat.Chatroom;
import com.chat.ChatroomRepository;
import com.chat.ChatroomSearchCriteria;
import com.chat.User;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 8:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class InMemoryChatroomRepository implements ChatroomRepository, Serializable {

    // Changing this will make files serialized before the change incompatible
    private static final long serialVersionUID = -6470090944414208496L;

    // We have lots of threads accessing this repository
    // We need to keep nextChatroomId and the map in sync
    private AtomicLong nextChatroomId = new AtomicLong(1);
    private Map<Long, Chatroom> chatroomIdMap = new ConcurrentHashMap<>();
    private Map<Chatroom, Set<User>> chatroomUserMap = new ConcurrentHashMap<>();

    transient private final Logger log = LogManager.getLogger();

    @Override
    public Chatroom createChatroom(User owner, String name, long latitude, long longitude, long radius, boolean isPrivate) {
        Chatroom chatroom = new Chatroom(nextChatroomId.getAndIncrement(), name, owner, this, latitude,
                longitude, radius, isPrivate, new LCMVChatroomActivity());
        addChatroom(chatroom);
        return chatroom;
    }

    @Override
    public Iterator<Chatroom> search(ChatroomSearchCriteria search) {
        Iterator<Chatroom> chatrooms = chatroomIdMap.values().iterator();
//        Vector<Chatroom> ret = new Vector<>();
//
//        while (chatrooms.hasNext()) {
//            Chatroom c = chatrooms.next();
//            long distMeters = distanceBetweenCoords(search.getLatitude(), search.getLongitude(), c.getLatitude(), c.getLongitude());
//            boolean canJoin = !search.returnOnlyJoinable() || distMeters <= c.getRadius() || c.getRadius() <= 0;
//            if (canJoin && distMeters <= search.getMetersFromCoords()) {
//
//                // !!! TODO (FIX)
//                // THIS IS A QUICK FIX TO EMPTY CHAT DELETION
//                // !!! TODO (FIX)
//                if (c.getUserCount() > 0) {
//                    ret.add(c);
//                }
//                else {
//                    removeChatroom(c.getId());
//                }
//            }
//        }

        //return ret.iterator();
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
    public void removeChatroom(long chatroomID) {
        Chatroom c = chatroomIdMap.get(chatroomID);
        chatroomUserMap.remove(c);
        chatroomIdMap.remove(chatroomID);
    }

    @Override
    public Iterator<Chatroom> iterator() {
        return chatroomIdMap.values().iterator();
    }

    @Override
    public int getChatroomUserCount(Chatroom chatroom) {
        return chatroomUserMap.get(chatroom).size();
    }

    @Override
    public Iterator<User> getUsers(Chatroom chatroom) {
        return chatroomUserMap.get(chatroom).iterator();
    }

    @Override
    public void addUser(Chatroom chatroom, User user) {
        log.debug("[chat repo] add user");
        chatroomUserMap.get(chatroom).add(user);
    }

    @Override
    public void removeUser(Chatroom chatroom, User user) {
        if (chatroomUserMap.get(chatroom).contains(user)) {
            log.info("[chatroom repo] removing user");
            chatroomUserMap.get(chatroom).remove(user);
        }
    }

    @Override
    public boolean containsUser(Chatroom chatroom, User sender) {
        Iterator<User> users = chatroomUserMap.get(chatroom).iterator();
        while (users.hasNext()) {
            log.debug("User {} in chatroom {}", users.next(), chatroom);
        }
        return chatroomUserMap.get(chatroom).contains(sender);
    }

    // This really shouldn't be here, but I really don't want to create a whole
    // utility class for this function that will only be used here.
    private long distanceBetweenCoords(long lat1, long lon1, long lat2, long lon2) {
        double earthRadius = 6371.01; // Earth's radius in Kilometers

        // Get the difference between our two points then convert the difference into radians
        double nDLat = (lat1 - lat2) * Math.PI / 180.0;
        double nDLon = (lon1 - lon2) * Math.PI / 180.0;

        double fromLat =  lat2 * Math.PI / 180.0;
        double toLat =  lat1 * Math.PI / 180.0;

        double nA =	Math.pow ( Math.sin(nDLat/2), 2 ) + Math.cos(fromLat) * Math.cos(toLat) * Math.pow ( Math.sin(nDLon/2), 2 );

        double nC = 2 * Math.atan2( Math.sqrt(nA), Math.sqrt( 1 - nA ));
        double nD = earthRadius * nC;

        return (long)(nD * 1000.); // Return our calculated distance in meters
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // perform the default serialization for all non-transient, non-static fields
        out.defaultWriteObject();

        out.writeLong(nextChatroomId.get());
        out.writeObject(chatroomIdMap);
        out.writeObject(chatroomUserMap);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // always perform the default de-serialization first
        in.defaultReadObject();

        nextChatroomId = new AtomicLong(in.readLong());
        chatroomIdMap = new ConcurrentHashMap<>((ConcurrentHashMap<Long, Chatroom>)in.readObject());
        chatroomUserMap = new ConcurrentHashMap<>((ConcurrentHashMap<Chatroom, Set<User>>)in.readObject());
    }
}

