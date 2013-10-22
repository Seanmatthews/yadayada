package com.chat.client;

import com.chat.*;
import com.chat.client.gui.ChatGUI;
import com.chat.server.impl.SocketConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientMessageSender {
    private final ChatClient client;
    private final Connection connection;

    public ClientMessageSender(ChatClient client, Connection connection) {
        this.client = client;
        this.connection = connection;
    }

    public void registerAndLogin(String user, String password) {
        try {
            registerNewUser(user, password);
            loginUser(user, password);
        } catch (IOException e) {
            System.out.println("Error registering and logging in");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void searchChatrooms() throws IOException {
        System.out.println("Searching for chatrooms");

        connection.writeShort(1);
        connection.writeByte(MessageTypes.SEARCH_CHATROOMS.getValue());
    }

    private void loginUser(String login, String password) throws IOException {
        System.out.println("Logging in user: " + login);
        connection.writeShort(1 + Utilities.getStringLength(login) + Utilities.getStringLength(password));
        connection.writeByte(MessageTypes.LOGIN.getValue());
        connection.writeString(login);
        connection.writeString(password);

        connection.readShort(); // size
        MessageTypes msgType = MessageTypes.lookup(connection.readByte());
        switch(msgType) {
            case LOGIN_ACCEPT:
                long userId = connection.readLong();
                User user = new User(userId, login, password, login);
                client.onUserLoggedIn(user);
                System.out.println("Login accepted. UserId: " + userId);
                break;
            case LOGIN_REJECT:
                String msg = connection.readString();
                System.out.println("Login rejected: " + msg);
                break;
        }
    }

    private void registerNewUser(String user, String password) throws IOException {
        System.out.println("Registering user: " + user);
        connection.writeShort(1 + Utilities.getStringLength(user) + Utilities.getStringLength(password) + Utilities.getStringLength(user));
        connection.writeByte(MessageTypes.REGISTER.getValue());
        connection.writeString(user);
        connection.writeString(password);
        connection.writeString(user);

        connection.readShort(); // size
        MessageTypes msgType = MessageTypes.lookup(connection.readByte());
        switch(msgType) {
            case REGISTER_ACCEPT:
                connection.readLong();
                System.out.println("Registration accepted. UserId: " + user);
                break;
            case REGISTER_REJECT:
                String msg = connection.readString();
                System.out.println("Failed to register user: " + msg);
                break;
        }
    }

    public void joinChatroom(User user, Chatroom chatroom) throws IOException {
        System.out.println("Joining chatroom: " + chatroom);
        connection.writeShort(17);
        connection.writeByte(MessageTypes.JOIN_CHATROOM.getValue());
        connection.writeLong(user.getId());
        connection.writeLong(chatroom.getId());
    }

    public void sendMessage(User user, Chatroom chatroom, String textToSend) throws IOException {
        System.out.println("Sending message: " + textToSend);
        connection.writeShort(1 + 8 + 8 + Utilities.getStringLength(textToSend));
        connection.writeByte(MessageTypes.SUBMIT_MESSAGE.getValue());
        connection.writeLong(user.getId());
        connection.writeLong(chatroom.getId());
        connection.writeString(textToSend);
    }

    public void createChatroom(User user, String chatroomName) throws IOException {
        System.out.println("Creating chatroom: " + chatroomName);
        connection.writeShort(1 + 8 + Utilities.getStringLength(chatroomName));
        connection.writeByte(MessageTypes.CREATE_CHATROOM.getValue());
        connection.writeLong(user.getId());
        connection.writeString(chatroomName);
    }

    public void leaveChatroom(User user, Chatroom chatroom) throws IOException {
        System.out.println("Leaving chatroom: " + chatroom);
        connection.writeShort(17);
        connection.writeByte(MessageTypes.LEAVE_CHATROOM.getValue());
        connection.writeLong(user.getId());
        connection.writeLong(chatroom.getId());
    }
}
