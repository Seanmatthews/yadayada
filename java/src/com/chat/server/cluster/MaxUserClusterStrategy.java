package com.chat.server.cluster;

import com.chat.ChatMessage;
import com.chat.Chatroom;
import com.chat.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 3/22/14
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class MaxUserClusterStrategy implements ClusteringStrategy {
    private final Logger log = LogManager.getLogger();

    private final Map<User, SimpleCluster> userClusterMap = new HashMap<>();
    private final List<SimpleCluster> clusters = new ArrayList<>();

    private final Chatroom chatroom;
    private final int maxUsers;
    private final int minUsers;

    private int nextCluster = 0;

    public MaxUserClusterStrategy(Chatroom chatroom, int minUsers, int maxUsers) {
        this.chatroom = chatroom;
        this.maxUsers = maxUsers;
        this.minUsers = minUsers;
        this.clusters.add(new SimpleCluster(2 * maxUsers));
    }

    @Override
    public ChatroomCluster addUser(User user) {
        recluster();

        return addToNextCluster(user);
    }

    @Override
    public void removeUser(User user) {
        SimpleCluster simpleCluster = userClusterMap.get(user);

        if (simpleCluster != null) {
            simpleCluster.removeUser(user);
            recluster();
        }
    }

    @Override
    public ChatroomCluster addMessage(ChatMessage message) {
        return addUser(message.getSender());
    }

    private void recluster() {
        int numUsers = userClusterMap.size();
        int usersPerCluster = numUsers / clusters.size();

        if (usersPerCluster > maxUsers || (usersPerCluster < minUsers && clusters.size() > 1)) {
            int newClusters = (int) Math.ceil(1.0 * numUsers / maxUsers);
            clusters.clear();

            for (int i=0; i<newClusters; i++)
                clusters.add(new SimpleCluster(2 * maxUsers));

            nextCluster = 0;
            Iterator<User> users = chatroom.getUsers();
            while(users.hasNext()) {
                User next = users.next();
                addToNextCluster(next);
            }
        }
    }

    private ChatroomCluster addToNextCluster(User next) {
        SimpleCluster simpleCluster = clusters.get(nextCluster++ % clusters.size());
        simpleCluster.addUser(next);
        return simpleCluster;
    }
}
