package com.chat.server;

import com.chat.BinaryStream;
import com.chat.Chatroom;
import com.chat.User;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatServer {
    void addConnection(BinaryStream sender);
    void removeConnection(BinaryStream sender);

    void registerUser(BinaryStream sender, String login, String password, String handle);
    void login(BinaryStream sender, String login, String password);

    void createChatroom(BinaryStream sender, User user, String name);
    void newMessage(BinaryStream sender, User senderUser, Chatroom chatroom, String message);

    void searchChatrooms(BinaryStream sender);
    void joinChatroom(BinaryStream sender, User user, Chatroom chatroom);
    void leaveChatroom(BinaryStream senderConnection, User sender, Chatroom chatroom, boolean removing);
}
