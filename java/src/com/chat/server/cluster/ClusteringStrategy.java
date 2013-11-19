package com.chat.server.cluster;

import com.chat.ChatMessage;
import com.chat.User;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/19/13
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ClusteringStrategy {
    ChatroomCluster addUser(User user);
    void removeUser(User user);
    ChatroomCluster addMessage(ChatMessage message);
}
