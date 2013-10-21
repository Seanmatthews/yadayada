package com.chat.server;

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
    void removeConnection(ChatClientSender sender);

    void registerUser(ChatClientSender sender, String login, String password);
    void login(ChatClientSender sender, String login, String password);

    void createChatroom(ChatClientSender sender, User user, String name);
    void newMessage(ChatClientSender sender, User senderUser, Chatroom chatroom, String message);

    void searchChatrooms(ChatClientSender sender);
    void joinChatroom(ChatClientSender sender, User user, Chatroom chatroom);
    void leaveChatroom(ChatClientSender sender, User user, Chatroom chatroom);
}
