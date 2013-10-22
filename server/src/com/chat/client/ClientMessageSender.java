package com.chat.client;

import com.chat.*;

import java.io.IOException;

import static com.chat.Utilities.getStrLen;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientMessageSender {
    private final ChatClient client;
    private final BinaryStream connection;

    public ClientMessageSender(ChatClient client, BinaryStream connection) {
        this.client = client;
        this.connection = connection;
    }

    public void registerAndLogin(String user, String password) {
        try {
            connect();
            registerNewUser(user, password);
            loginUser(user, password);
        } catch (IOException e) {
            System.out.println("Error registering and logging in");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void connect() {
        try {
            String UUID = "FOO";
            connection.startWriting(1 + 4 + getStrLen(UUID));
            connection.writeByte(MessageTypes.CONNECT.getValue());
            connection.writeInt(1);
            connection.writeString(UUID);
            connection.finishWriting();

            connection.startReading();
            if (MessageTypes.lookup(connection.readByte()) != MessageTypes.CONNECT_ACCEPT) {
                System.out.println("Did not accept connection. WTF!");
                System.exit(0);
            }
            connection.finishReading();
        } catch (IOException e) {
            System.out.println("Error connecting");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void searchChatrooms() throws IOException {
        System.out.println("Searching for chatrooms");

        connection.startWriting(1);
        connection.writeByte(MessageTypes.SEARCH_CHATROOMS.getValue());
        connection.finishWriting();
    }

    private void loginUser(String login, String password) throws IOException {
        System.out.println("Logging in user: " + login);
        connection.startWriting(1 + getStrLen(login) + getStrLen(password));
        connection.writeByte(MessageTypes.LOGIN.getValue());
        connection.writeString(login);
        connection.writeString(password);
        connection.finishWriting();

        connection.startReading();
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

        connection.finishReading();
    }

    private void registerNewUser(String user, String password) throws IOException {
        System.out.println("Registering user: " + user);
        /*connection.startWriting(1 + getStrLen(user) + getStrLen(password) + getStrLen(user));
        connection.writeByte(MessageTypes.REGISTER.getValue());
        connection.writeString(user);
        connection.writeString(password);
        connection.writeString(user);
        connection.finishWriting();*/

        // Quick Register
        connection.startWriting(1 + getStrLen(user));
        connection.writeByte(MessageTypes.QUICK_REGISTER.getValue());
        connection.writeString(user);
        connection.finishWriting();

        connection.startReading();
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

        connection.finishReading();
    }

    public void joinChatroom(User user, Chatroom chatroom) throws IOException {
        System.out.println("Joining chatroom: " + chatroom);
        connection.startWriting(17);
        connection.writeByte(MessageTypes.JOIN_CHATROOM.getValue());
        connection.writeLong(user.getId());
        connection.writeLong(chatroom.getId());
        connection.finishWriting();
    }

    public void sendMessage(User user, Chatroom chatroom, String textToSend) throws IOException {
        System.out.println("Sending message: " + textToSend);
        connection.startWriting(1 + 8 + 8 + getStrLen(textToSend));
        connection.writeByte(MessageTypes.SUBMIT_MESSAGE.getValue());
        connection.writeLong(user.getId());
        connection.writeLong(chatroom.getId());
        connection.writeString(textToSend);
        connection.finishWriting();
    }

    public void createChatroom(User user, String chatroomName) throws IOException {
        System.out.println("Creating chatroom: " + chatroomName);
        connection.startWriting(1 + 8 + getStrLen(chatroomName));
        connection.writeByte(MessageTypes.CREATE_CHATROOM.getValue());
        connection.writeLong(user.getId());
        connection.writeString(chatroomName);
        connection.finishWriting();
    }

    public void leaveChatroom(User user, Chatroom chatroom) throws IOException {
        System.out.println("Leaving chatroom: " + chatroom);
        connection.startWriting(17);
        connection.writeByte(MessageTypes.LEAVE_CHATROOM.getValue());
        connection.writeLong(user.getId());
        connection.writeLong(chatroom.getId());
        connection.finishWriting();
    }
}
