package com.chat.server;

import com.chat.*;

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
public class ChatServerCoordinator implements ChatServer {
    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;

    private final Map<User, Connection> userConnectionMap;
    private final Map<Connection, User> connectionUserMap;

    public ChatServerCoordinator(UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) {
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
        this.userConnectionMap =  new ConcurrentHashMap<>();
        this.connectionUserMap = new ConcurrentHashMap<>();
    }

    @Override
    public void removeConnection(Connection connection) {
        System.out.println("Removing connection to " + connection);

        User user = connectionUserMap.remove(connection);

        if (user != null)
            userConnectionMap.remove(user);

        connection.close();
    }

    @Override
    public void newMessage(Connection sendingConnection, User sender, Chatroom chatroom, String message) {
        System.out.println("New message from " + sender + " " + message);

        Message msg = messageRepo.create(chatroom, sender, message);
        chatroom.addMessage(msg);

        Iterator<User> chatUsers = chatroom.getUsers();
        while (chatUsers.hasNext()) {
            User user = chatUsers.next();
            Connection connection = userConnectionMap.get(user);
            if (connection != null) {
                try {
                    writeMessage(connection, msg);
                } catch (IOException e) {
                    removeConnection(connection);
                }
            }
        }
    }

    private void writeMessage(Connection connection, Message msg) throws IOException {
        System.out.println("Sending message " + msg);

        connection.writeShort(1 + 8 + 8 + Utilities.getStringLength(msg.sender.login) + Utilities.getStringLength(msg.message));
        connection.writeByte(MessageTypes.MESSAGE.getValue());
        connection.writeLong(msg.id);
        connection.writeLong(msg.sender.id);
        connection.writeLong(msg.chatroom.id);
        connection.writeString(msg.sender.login);
        connection.writeString(msg.message);
    }

    @Override
    public void createChatroom(Connection connection, User user, String name) {
        System.out.println("Creating chatroom " + name + " by " + user);

        Chatroom chatroom = chatroomRepo.createChatroom(user, name);
        sendChatroom(connection, chatroom);
    }

    @Override
    public void registerUser(Connection connection, String login, String password) {
        System.out.println("Registering user " + login);

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
        System.out.println("Logging in user " + login);

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
        System.out.println("Searching chatrooms " + connection);

        Iterator<Chatroom> chatrooms = chatroomRepo.search(new ChatroomSearchCriteria());

        while(chatrooms.hasNext()) {
            sendChatroom(connection, chatrooms.next());
        }
    }

    private void sendChatroom(Connection connection, Chatroom chatroom) {
        System.out.println("Send chatroom " + chatroom);

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

        try {
            Iterator<User> users = chatroom.getUsers();
            while(users.hasNext()) {
                User next = users.next();

                connection.writeShort(1 + 8 + 8 + Utilities.getStringLength(next.login));
                connection.writeByte(MessageTypes.JOINED_CHATROOM.getValue());
                connection.writeLong(chatroom.id);
                connection.writeLong(next.id);
                connection.writeString(next.login);
            }

            Iterator<Message> recentMessages = chatroom.getRecentMessages();
            while(recentMessages.hasNext()) {
                writeMessage(connection, recentMessages.next());
            }
        } catch (IOException e) {
            removeConnection(connection);
        }
    }

     @Override
    public void leaveChatroom(Connection connection, User user, Chatroom chatroom) {
        System.out.println("Removing " + user.login + " from " + chatroom.name);
        chatroom.removeUser(user);

        // TODO: send a response?
    }
}
