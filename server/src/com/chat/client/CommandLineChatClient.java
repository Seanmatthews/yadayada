package com.chat.client;

import com.chat.*;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommandLineChatClient implements ChatClient {
    private final Socket socket;
    private final ClientConnection connection;
    private final DataOutputStream dout;

    private Chatroom subscribedChatroom;
    private User user;

    public CommandLineChatClient(String host, int port, String user, String password) throws IOException, InterruptedException {
        socket = new Socket(host, port);

        System.out.println("Connected to " + socket);

        DataInputStream din = new DataInputStream(socket.getInputStream());
        dout = new DataOutputStream(socket.getOutputStream());

        InMemoryChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        InMemoryUserRepository userRepo = new InMemoryUserRepository();

        connection = new ClientConnection(this, din, dout);
        connection.registerAndLogin(user, password);
        connection.searchChatrooms();

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatClientInput(this, chatroomRepo, userRepo));
        pool.submit(new ChatClientListener(this, din, chatroomRepo, userRepo));
    }

    @Override
    public void onChatroom(Chatroom chatroom) throws IOException {
        System.out.println("New chatroom: " + chatroom.name + " by " + chatroom.owner.login);

        // Subscribe to the first one!
        if (chatroom.name.equalsIgnoreCase("Global")) {
            dout.writeShort(1 + 8 + 8);
            dout.writeByte(MessageTypes.JOIN_CHATROOM.getValue());
            dout.writeLong(user.id);
            dout.writeLong(chatroom.id);

            subscribedChatroom = chatroom;
        }
    }

    @Override
    public void onMessage(Message message) {
        System.out.println(message.chatroom.name + " " + message.sender.login + ": " + message.message);
    }

    @Override
    public void sendMessage(String message) throws IOException {
        dout.writeShort(1 + 8 + Utilities.getStringLength(message));
        dout.writeByte(MessageTypes.SUBMIT_MESSAGE.getValue());
        dout.writeLong(user.id);
        dout.writeLong(subscribedChatroom.id);
        dout.writeUTF(message);
    }

    @Override
    public void onUserLoggedIn(User user) {
        this.user = user;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(args[1]);
        new CommandLineChatClient(args[0], port, args[2], args[3]);
    }
}
