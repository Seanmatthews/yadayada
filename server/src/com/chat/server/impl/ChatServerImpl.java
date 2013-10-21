package com.chat.server.impl;

import com.chat.*;
import com.chat.server.ChatClientSender;
import com.chat.server.ChatServer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatServerImpl implements ChatServer {
    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;

    private final Map<User, ChatClientSender> userConnectionMap;
    private final Map<ChatClientSender, User> connectionUserMap;

    public ChatServerImpl(UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) {
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
        this.userConnectionMap =  new ConcurrentHashMap<>();
        this.connectionUserMap = new ConcurrentHashMap<>();
    }

    @Override
    public void removeConnection(ChatClientSender connection) {
        System.out.println("Removing connection to " + connection);

        User user = connectionUserMap.remove(connection);

        if (user != null)
            userConnectionMap.remove(user);

        connection.close();
    }

    @Override
    public void newMessage(ChatClientSender sendingConnection, User sender, Chatroom chatroom, String message) {
        System.out.println("New message from " + sender + " " + message);

        Message msg = messageRepo.create(chatroom, sender, message);
        chatroom.addMessage(msg);

        Iterator<User> chatUsers = chatroom.getUsers();
        while (chatUsers.hasNext()) {
            User user = chatUsers.next();
            ChatClientSender connection = userConnectionMap.get(user);

            if (connection != null) {
                try {
                     connection.sendMessage(msg);
                } catch (IOException e) {
                    removeConnection(connection);
                }
            }
        }
    }

    @Override
    public void createChatroom(ChatClientSender connection, User user, String name) {
        System.out.println("Creating chatroom " + name + " by " + user);

        Chatroom chatroom = chatroomRepo.createChatroom(user, name);

        try {
            connection.sendChatroom(chatroom);
        } catch (IOException e) {
            removeConnection(connection);
        }
    }

    @Override
    public void registerUser(ChatClientSender connection, String login, String password) {
        System.out.println("Registering user " + login);

        User user = userRepo.registerUser(login, password);

        try {
            if (user == null) {
                connection.sendRegisterReject(user, "Registration failure. " + login + " already exists");
            }
            else {
                connection.sendRegisterAccept(user);
            }
        } catch (IOException e) {
            removeConnection(connection);
        }
    }

    @Override
    public void login(ChatClientSender connection, String login, String password) {
        System.out.println("Logging in user " + login);

        User user = userRepo.login(login, password);

        try {
            if (user == null) {
                connection.sendLoginReject("Invalid user or password: " + login);
                return;
            }

            connection.sendLoginAccept(user);

            userConnectionMap.put(user, connection);
            connectionUserMap.put(connection, user);
        } catch (IOException e) {
            removeConnection(connection);
        }
    }

    @Override
    public void searchChatrooms(ChatClientSender connection) {
        System.out.println("Searching chatrooms " + connection);

        Iterator<Chatroom> chatrooms = chatroomRepo.search(new ChatroomSearchCriteria());

        while(chatrooms.hasNext()) {
            try {
                connection.sendChatroom(chatrooms.next());
            } catch (IOException e) {
                removeConnection(connection);
            }
        }
    }

    @Override
    public void joinChatroom(ChatClientSender connection, User user, Chatroom chatroom) {
        System.out.println("Adding " + user.getLogin() + " to " + chatroom.getName());
        chatroom.addUser(user);

        try {
            Iterator<User> users = chatroom.getUsers();
            while(users.hasNext()) {
                User next = users.next();
                connection.sendJoinChatroom(chatroom, next);
            }

            Iterator<Message> recentMessages = chatroom.getRecentMessages();
            while(recentMessages.hasNext()) {
                connection.sendMessage(recentMessages.next());
            }
        } catch (IOException e) {
            removeConnection(connection);
        }
    }

    @Override
    public void leaveChatroom(ChatClientSender connection, User user, Chatroom chatroom) {
        System.out.println("Removing " + user.getLogin() + " from " + chatroom.getName());
        chatroom.removeUser(user);

        // TODO: send a response?
    }
}
