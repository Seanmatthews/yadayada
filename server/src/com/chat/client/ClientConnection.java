package com.chat.client;

import com.chat.*;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientConnection {
    private final DataInputStream din;
    private final DataOutputStream dout;
    private final ChatClient client;

    public ClientConnection(ChatClient client,
                            DataInputStream din,
                            DataOutputStream dout) throws IOException {
        this.client = client;
        this.din = din;
        this.dout = dout;
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

        dout.writeShort(1);
        dout.writeByte(MessageTypes.SEARCH_CHATROOMS.getValue());
    }

    private void loginUser(String login, String password) throws IOException {
        System.out.println("Logging in user: " + login);
        dout.writeShort(1 + Utilities.getStringLength(login) + Utilities.getStringLength(password));
        dout.writeByte(MessageTypes.LOGIN.getValue());
        dout.writeUTF(login);
        dout.writeUTF(password);

        din.readShort(); // size
        MessageTypes msgType = MessageTypes.lookup(din.readByte());
        switch(msgType) {
            case LOGIN_ACCEPT:
                long userId = din.readLong();
                User user = new User();
                user.id = userId;
                user.login = login;
                client.onUserLoggedIn(user);
                System.out.println("Login accepted. UserId: " + userId);
                break;
            case LOGIN_REJECT:
                String msg = din.readUTF();
                System.out.println("Login rejected: " + msg);
                break;
        }
    }

    private void registerNewUser(String user, String password) throws IOException {
        System.out.println("Registering user: " + user);
        dout.writeShort(1 + Utilities.getStringLength(user) + Utilities.getStringLength(password));
        dout.writeByte(MessageTypes.REGISTER.getValue());
        dout.writeUTF(user);
        dout.writeUTF(password);

        din.readShort(); // size
        MessageTypes msgType = MessageTypes.lookup(din.readByte());
        switch(msgType) {
            case REGISTER_ACCEPT:
                din.readLong();
                System.out.println("Registration accepted. UserId: " + user);
                break;
            case REGISTER_REJECT:
                String msg = din.readUTF();
                System.out.println("Failed to register user: " + msg);
                break;
        }
    }

    public void joinChatroom(User user, Chatroom chatroom) throws IOException {
        System.out.println("Joining chatroom: " + chatroom);
        dout.writeShort(17);
        dout.writeByte(MessageTypes.JOIN_CHATROOM.getValue());
        dout.writeLong(user.id);
        dout.writeLong(chatroom.id);
    }

    public void sendMessage(User user, Chatroom chatroom, String textToSend) throws IOException {
        System.out.println("Sending message: " + textToSend);
        dout.writeShort(1 + 8 + 8 + Utilities.getStringLength(textToSend));
        dout.writeByte(MessageTypes.SUBMIT_MESSAGE.getValue());
        dout.writeLong(user.id);
        dout.writeLong(chatroom.id);
        dout.writeUTF(textToSend);
    }

    public void createChatroom(User user, String chatroomName) throws IOException {
        System.out.println("Creating chatroom: " + chatroomName);
        dout.writeShort(1 + 8 + Utilities.getStringLength(chatroomName));
        dout.writeByte(MessageTypes.CREATE_CHATROOM.getValue());
        dout.writeLong(user.id);
        dout.writeUTF(chatroomName);
    }
}
