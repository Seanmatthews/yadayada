package com.chat.impl;

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
public class STChatroomRepository implements ChatroomRepository {
    // We have lots of threads accessing this repository
    // We need to keep nextChatroomId and the map in sync
    private long nextChatroomId = 1;
    private final Map<Long, Chatroom> chatroomIdMap = new HashMap<>();
    private final Map<Chatroom, Set<User>> chatroomUserMap = new HashMap<>();

    @Override
    public Chatroom createChatroom(User owner, String name, long latitude, long longitude, long radius) {
        Chatroom chatroom = new Chatroom(nextChatroomId++, name, owner, this, latitude, longitude, radius);
        addChatroom(chatroom);
        return chatroom;
    }

    @Override
    public Iterator<Chatroom> search(ChatroomSearchCriteria search) {
        Iterator<Chatroom> chatrooms = chatroomIdMap.values().iterator();
        Vector<Chatroom> ret = new Vector<>();

        while (chatrooms.hasNext()) {
            Chatroom c = chatrooms.next();
            long distMeters = distanceBetweenCoords(search.getLatitude(), search.getLongitude(), c.getLatitude(), c.getLongitude());
            boolean canJoin = !search.returnOnlyJoinable() || distMeters <= c.getRadius() || c.getRadius() <= 0;
            if (canJoin && distMeters <= search.getMetersFromCoords()) {

                // !!! TODO (FIX)
                // THIS IS A QUICK FIX TO EMPTY CHAT DELETION
                // !!! TODO (FIX)
                if (c.getUserCount() > 0) {
                    ret.add(c);
                }
                else {
                    removeChatroom(c.getId());
                }
            }
        }

        return ret.iterator();
        //return chatroomIdMap.values().iterator();
    }

    @Override
    public Chatroom get(Long chatroomId) {
        return chatroomIdMap.get(chatroomId);
    }

    @Override
    public void addChatroom(Chatroom chatroom) {
        chatroomIdMap.put(chatroom.getId(), chatroom);
        chatroomUserMap.put(chatroom, new HashSet<User>());
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
        chatroomUserMap.get(chatroom).add(user);
    }

    @Override
    public void removeUser(Chatroom chatroom, User user) {
        chatroomUserMap.get(chatroom).remove(user);
    }

    @Override
    public boolean containsUser(Chatroom chatroom, User user) {
        return chatroomUserMap.get(chatroom).contains(user);
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

        double nA =	pow ( sin(nDLat/2), 2 ) + cos(fromLat) * cos(toLat) * pow ( sin(nDLon/2), 2 );

        double nC = 2 * atan2( sqrt(nA), sqrt( 1 - nA ));
        double nD = earthRadius * nC;

        return (long)(nD * 1000.); // Return our calculated distance in meters
    }
}
