package com.chat.client.text;

import com.chat.*;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientListener;
import com.chat.client.ClientConnection;
import com.chat.server.impl.InMemoryChatroomRepository;
import com.chat.server.impl.InMemoryUserRepository;
import com.chat.server.impl.SocketConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:50 AM
 * To change this template use File | Settings | File Templates.
 */public class ChatTextClient implements ChatClient {
    private final ClientConnection connection;
    private final Connection dout;

    private Chatroom subscribedChatroom;
    private User user;

    public ChatTextClient(String host, int port, String user, String password) throws IOException, InterruptedException {
        Socket socket = new Socket(host, port);
        dout = new SocketConnection(socket);

        System.out.println("Connected to " + socket);

        InMemoryChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        InMemoryUserRepository userRepo = new InMemoryUserRepository();

        connection = new ClientConnection(this, dout);
        connection.registerAndLogin(user, password);
        connection.searchChatrooms();

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatTextInput(this));
        pool.submit(new ChatClientListener(this, dout, chatroomRepo, userRepo));
    }

    @Override
    public void onChatroom(Chatroom chatroom) throws IOException {
        System.out.println("New chatroom: " + chatroom.getName() + " by " + chatroom.getOwner().getHandle());

        // Subscribe to the first one!
        if (chatroom.getName().equalsIgnoreCase("Global")) {
            dout.writeShort(1 + 8 + 8);
            dout.writeByte(MessageTypes.JOIN_CHATROOM.getValue());
            dout.writeLong(user.getId());
            dout.writeLong(chatroom.getId());

            subscribedChatroom = chatroom;
        }
    }

    @Override
    public void onMessage(Message message) {
        System.out.println(message.getChatroom().getName() + " " + message.getSender().getHandle() + ": " + message.getMessage());
    }

    @Override
    public void sendMessage(String message) throws IOException {
        dout.writeShort(1 + 8 + Utilities.getStringLength(message));
        dout.writeByte(MessageTypes.SUBMIT_MESSAGE.getValue());
        dout.writeLong(user.getId());
        dout.writeLong(subscribedChatroom.getId());
        dout.writeString(message);
    }

    @Override
    public void onJoinedChatroom(Chatroom chat, User user) {
        System.out.println(user + " has joined " + chat);
    }

    @Override
    public void onUserLoggedIn(User user) {
        this.user = user;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(args[1]);
        new ChatTextClient(args[0], port, args[2], args[3]);
    }
}
