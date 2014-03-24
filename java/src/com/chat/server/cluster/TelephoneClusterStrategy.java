package com.chat.server.cluster;

import com.chat.ChatMessage;
import com.chat.User;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 3/22/14
 * Time: 6:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class TelephoneClusterStrategy implements ClusteringStrategy {
    @Override
    public ChatroomCluster addUser(User user) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeUser(User user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ChatroomCluster addMessage(ChatMessage message) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
