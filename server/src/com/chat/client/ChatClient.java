package com.chat.client;

import com.chat.Chatroom;
import com.chat.Message;
import com.chat.User;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatClient {
    void onUserLoggedIn(User user);
    void onChatroom(Chatroom chatroom) throws IOException;
    void onMessage(Message message);
    void sendMessage(String msg) throws IOException;
    void onJoinedChatroom(Chatroom chat, User user);
}
