package com.chat.server;

import com.chat.*;
import com.chat.server.ChatServer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatServerCoordinator implements ChatServer {
    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;

    private final Map<User, Connection> userConnectionMap;
    private final Map<Connection, User> connectionUserMap;

    public ChatServerCoordinator(UserRepository userRepo,
                                 ChatroomRepository chatroomRepo,
                                 MessageRepository messageRepo) {
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
        this.userConnectionMap =  new ConcurrentHashMap<>();
        this.connectionUserMap = new ConcurrentHashMap<>();
    }

    @Override
    public void removeConnection(Connection connection) {
        User user = connectionUserMap.remove(connection);

        if (user != null)
            userConnectionMap.remove(user);

        System.out.println("Removing connection to " + connection);
        connection.close();
    }

    @Override
    public void newMessage(Connection sendingConnection, User sender, Chatroom chatroom, String message) {
        Message msg = messageRepo.create(chatroom, sender, message);

        Iterator<User> chatUsers = chatroom.getUsers();
        while (chatUsers.hasNext()) {
            User user = chatUsers.next();
            Connection connection = userConnectionMap.get(user);
            if (connection != null) {
                try {
                    connection.writeShort(1 + 8 + 8 + Utilities.getStringLength(sender.login) + Utilities.getStringLength(msg.message));
                    connection.writeByte(MessageTypes.MESSAGE.getValue());
                    connection.writeLong(msg.id);
                    connection.writeLong(msg.sender.id);
                    connection.writeLong(msg.chatroom.id);
                    connection.writeString(msg.sender.login);
                    connection.writeString(msg.message);
                } catch (IOException e) {
                    removeConnection(connection);
                }
            }
        }
    }

    @Override
    public void createChatroom(Connection connection, User user, String name) {
        Chatroom chatroom = chatroomRepo.createChatroom(user, name);
        sendChatroom(connection, chatroom);
    }

    @Override
    public void registerUser(Connection connection, String login, String password) {
        User user = userRepo.registerUser(login, password);

        try {
            if (user == null) {
                String msg = "Registration failure. " + login + " already exists";
                connection.writeShort(1 + Utilities.getStringLength(msg));
                connection.writeByte(MessageTypes.REGISTER_REJECT.getValue());
                connection.writeString(msg);
            }
            else {
                connection.writeShort(1 + 8);
                connection.writeByte(MessageTypes.REGISTER_ACCEPT.getValue());
                connection.writeLong(user.id);
            }
        } catch (IOException e) {
            System.out.println("Error writing to client when registering user");
            removeConnection(connection);
        }
    }

    @Override
    public void login(Connection connection, String login, String password) {
        User user = userRepo.login(login, password);

        try {
            if (user == null) {
                String msg = "Invalid user or password: " + login;
                connection.writeShort(1 + Utilities.getStringLength(msg));
                connection.writeByte(MessageTypes.LOGIN_REJECT.getValue());
                connection.writeString(msg);
                return;
            }

            connection.writeShort(1 + 8);
            connection.writeByte(MessageTypes.LOGIN_ACCEPT.getValue());
            connection.writeLong(user.id);

            userConnectionMap.put(user, connection);
            connectionUserMap.put(connection, user);
        } catch (IOException e) {
            removeConnection(connection);
        }
    }

    @Override
    public void searchChatrooms(Connection connection) {
        List<Chatroom> chatrooms = chatroomRepo.search(new ChatroomSearchCriteria());

        for (Chatroom chatroom : chatrooms) {
            sendChatroom(connection, chatroom);
        }
    }

    private void sendChatroom(Connection connection, Chatroom chatroom) {
        try {
            int msgBytes = 1 + 8 + Utilities.getStringLength(chatroom.name) + 8 + Utilities.getStringLength(chatroom.owner.login);

            connection.writeShort(msgBytes);
            connection.writeByte(MessageTypes.CHATROOM.getValue());
            connection.writeLong(chatroom.id);
            connection.writeString(chatroom.name);
            connection.writeLong(chatroom.owner.id);
            connection.writeString(chatroom.owner.login);
        } catch (IOException e) {
            removeConnection(connection);
        }
    }

    @Override
    public void joinChatroom(Connection connection, User user, Chatroom chatroom) {
        System.out.println("Adding " + user.login + " to " + chatroom.name);
        chatroom.addUser(user);

        // TODO: send a response?
    }

     @Override
    public void leaveChatroom(Connection connection, User user, Chatroom chatroom) {
        System.out.println("Removing " + user.login + " from " + chatroom.name);
        chatroom.removeUser(user);

        // TODO: send a response?
    }
}
