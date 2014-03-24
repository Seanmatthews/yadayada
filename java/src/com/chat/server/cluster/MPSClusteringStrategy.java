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
 * Date: 11/19/13
 * Time: 12:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class MPSClusteringStrategy implements ClusteringStrategy {
    private final Logger log = LogManager.getLogger();

    private final Chatroom chatroom;
    private final double minMPS;
    private final double maxMPS;
    private final int[] mpsBuckets = new int[120];

    private final Map<User, SimpleCluster> userClusterMap = new HashMap<>();
    private final List<SimpleCluster> clusters = new ArrayList<>();

    private final Random random = new Random();

    private int numClusters = 1;
    private long lastSecond = 0;

    public MPSClusteringStrategy(Chatroom chatroom, double minMPS, double maxMPS) {
        this.chatroom = chatroom;
        this.minMPS = minMPS;
        this.maxMPS = maxMPS;

        this.numClusters = 1;
    }

    @Override
    public ChatroomCluster addUser(User user) {
        if (userClusterMap.size() == 0)
            recluster();

        SimpleCluster cluster = userClusterMap.get(user);

        if (cluster == null) {
            // make sure we're in a default cluster
            cluster = clusters.get(random.nextInt(numClusters));
            cluster.addUser(user);
            userClusterMap.put(user, cluster);
        }

        return cluster;
    }

    @Override
    public void removeUser(User user) {
        if (userClusterMap.size() == 0)
            recluster();

        SimpleCluster cluster = userClusterMap.remove(user);
        Iterator<User> users = cluster.getUsers();

        while(users.hasNext()) {
            if (users.next().equals(user)) {
                users.remove();
                break;
            }
        }
    }

    public ChatroomCluster addMessage(ChatMessage message) {
        long currentSecond = message.getTimestamp() / 1000;

        ChatroomCluster cluster = addUser(message.getSender());

        if (lastSecond != 0 && lastSecond != currentSecond) {
            for(long i=lastSecond + 1; i<currentSecond; i++) {
                // inefficient but easy to understand
                mpsBuckets[(int) (i % mpsBuckets.length)] = 0;
            }
            mpsBuckets[(int) (currentSecond % mpsBuckets.length)]++;

            double mps = calculateMessagesPerSecond();

            if (mps > maxMPS) {
                numClusters *= 2;
                log.info("Increasing chatroom {} clusters {} => {}. MPS: {}", chatroom, numClusters/2, numClusters, mps);
                recluster();
                cluster = userClusterMap.get(message.getSender());
            }
            else if (mps < minMPS && numClusters != 1) {
                numClusters /= 2;
                log.info("Decreasing chatroom {} clusters {} => {}. MPS: {}", chatroom, numClusters/2, numClusters, mps);
                recluster();
                cluster = userClusterMap.get(message.getSender());
            }
        }

        lastSecond = currentSecond;
        return cluster;
    }

    private void recluster() {
        Arrays.fill(mpsBuckets, 0);
        userClusterMap.clear();
        clusters.clear();

        for (int i=0; i<numClusters; i++) {
            clusters.add(new SimpleCluster(2 * chatroom.getUserCount() / numClusters));
        }

        Iterator<User> users = chatroom.getUsers();
        while(users.hasNext()) {
            User user = users.next();
            SimpleCluster cluster = clusters.get(random.nextInt(numClusters));
            cluster.addUser(user);
            userClusterMap.put(user, cluster);
        }
    }

    private double calculateMessagesPerSecond() {
        int total = 0;
        for (int mps : mpsBuckets)
            total += mps;
        return 1.0 * total / mpsBuckets.length;
    }
}
