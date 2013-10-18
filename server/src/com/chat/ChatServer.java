package com.chat;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatServer {
    void removeConnection(Connection socket);

    void registerUser(Connection socket, String login, String password);
    void login(Connection socket, String login, String password);

    void createChatroom(Connection socket, User user, String name);
    void newMessage(Connection connection, User sender, Chatroom chatroom, String message);

    void searchChatrooms(Connection socket);
    void joinChatroom(Connection socket, User user, Chatroom chatroom);
    void leaveChatroom(Connection socket, User user, Chatroom chatroom);
}
