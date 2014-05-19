package com.chat.server;

import com.chat.ClientConnection;
import com.chat.Chatroom;
import com.chat.User;

import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatServer {
    void connect(ClientConnection sender, int apiVersion, String uuid);
    void disconnect(ClientConnection sender);

    void registerUser(ClientConnection sender, String login, String password, String handle, String UUID,
                      long phoneNumber, String deviceTokenString);

    void login(ClientConnection sender, String login, String password);

    void quickLogin(ClientConnection sender, String handle, String UUID, long phoneNumber, String deviceTokenString);

    void streamReset(ClientConnection senderConnection, User user, Byte appAwake);

    void createChatroom(ClientConnection sender, User user, String name, long latitude, long longitude, long radius,
                        boolean isPrivate);

    void newMessage(ClientConnection sender, User senderUser, Chatroom chatroom, String message);

    void searchChatrooms(ClientConnection sender, long latitude, long longitude, long metersFromCoords,
                         byte onlyJoinable);

    void joinChatroom(ClientConnection sender, User user, Chatroom chatroom);

    void leaveChatroom(ClientConnection senderConnection, User sender, Chatroom chatroom);

    void inviteUser(ClientConnection sender, long chatroomId, long recipientId, long recipientPhone)
            throws ExecutionException, InterruptedException;

    void heartbeat(ClientConnection sender, long timestamp, long latitude, long longitude);

    void terminate(ClientConnection sender);
}
