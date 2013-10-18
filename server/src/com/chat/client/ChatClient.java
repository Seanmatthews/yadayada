package com.chat.client;

import com.chat.Chatroom;
import com.chat.MessageTypes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChatClient {
    private final Socket socket;
    private final DataInputStream din;
    private final DataOutputStream dout;
    private long userId;
    private Map<Long, Chatroom> chatroomIdToChatroom = new HashMap<Long, Chatroom>();
    private long subscribedChatroom;

    public ChatClient(String host, int port, String user, String password) throws IOException, InterruptedException {
        socket = new Socket(host, port);

        System.out.println("Connected to " + socket);

        din = new DataInputStream(socket.getInputStream());
        dout = new DataOutputStream(socket.getOutputStream());

        registerAndLogin(user, password);

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatClientInput(this));
        pool.submit(new ChatClientListener(this, din));
    }

    private void registerAndLogin(String user, String password) {
        try {
            registerNewUser(user, password);
            loginUser(user, password);
            searchChatrooms();
        } catch (IOException e) {
            System.out.println("Error registering and logging in");
            e.printStackTrace();
            System.exit(0);
        } catch (InterruptedException e) {
            System.out.println("Error registering and logging in");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void searchChatrooms() throws IOException, InterruptedException {
        System.out.println("Searching for chatrooms");

        dout.writeShort(1);
        dout.writeByte(MessageTypes.SEARCH_CHATROOMS.getValue());
    }

    private void loginUser(String user, String password) throws IOException {
        System.out.println("Logging in user: " + user);
        dout.writeShort(1 + getLength(user) + getLength(password));
        dout.writeByte(MessageTypes.LOGIN.getValue());
        dout.writeUTF(user);
        dout.writeUTF(password);

        din.readShort(); // size
        MessageTypes msgType = MessageTypes.lookup(din.readByte());
        switch(msgType) {
            case LOGIN_ACCEPT:
                this.userId = din.readLong();
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
        dout.writeShort(1 + getLength(user) + getLength(password));
        dout.writeByte(MessageTypes.REGISTER.getValue());
        dout.writeUTF(user);
        dout.writeUTF(password);

        din.readShort(); // size
        MessageTypes msgType = MessageTypes.lookup(din.readByte());
        switch(msgType) {
            case REGISTER_ACCEPT:
                this.userId = din.readLong();
                System.out.println("Registration accepted. UserId: " + user);
                break;
            case REGISTER_REJECT:
                String msg = din.readUTF();
                System.out.println("Failed to register user: " + msg);
                break;
        }
    }

    public void onChatroom(long chatroomId, String chatroomName, long ownerUserId, String ownerName) throws IOException {
        System.out.println("New chatroom: " + chatroomName + " by " + ownerName);

        Chatroom chatroom = new Chatroom();
        chatroom.id = chatroomId;
        chatroom.name = chatroomName;
        chatroomIdToChatroom.put(chatroomId, chatroom);

        // Subscribe to the first one!
        if (chatroomName.equalsIgnoreCase("Global")) {
            dout.writeShort(1 + 8 + 8);
            dout.writeByte(MessageTypes.JOIN_CHATROOM.getValue());
            dout.writeLong(userId);
            dout.writeLong(chatroomId);

            subscribedChatroom = chatroomId;
        }
    }

    public void onMessage(String userName, String message) {
        System.out.println(userName + ": " + message);
    }

    public void sendMessage(String message) throws IOException {
        dout.writeShort(1 + 8 + getLength(message));
        dout.writeByte(MessageTypes.SUBMIT_MESSAGE.getValue());
        dout.writeLong(userId);
        dout.writeLong(subscribedChatroom);
        dout.writeUTF(message);
    }

    private int getLength(String str) {
        return 2 + str.length();
    }
}
