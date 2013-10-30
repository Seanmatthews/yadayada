package com.chat.server;

import com.chat.Chatroom;
import com.chat.User;
import com.chat.msgs.v1.ClientConnection;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatServer {
    void addConnection(ClientConnection sender);
    void removeConnection(ClientConnection sender);

    void registerUser(ClientConnection sender, String login, String password, String handle);
    void login(ClientConnection sender, String login, String password);

    void createChatroom(ClientConnection sender, User user, String name);
    void newMessage(ClientConnection sender, User senderUser, Chatroom chatroom, String message);

    void searchChatrooms(ClientConnection sender);
    void joinChatroom(ClientConnection sender, User user, Chatroom chatroom);
    void leaveChatroom(ClientConnection sender, User user, Chatroom chatroom);
}
