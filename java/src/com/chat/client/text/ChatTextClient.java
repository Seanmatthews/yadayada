package com.chat.client.text;

import com.chat.*;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientDispatcher;
import com.chat.client.ChatClientUtilities;
import com.chat.msgs.V1Dispatcher;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.*;
import com.chat.impl.DataStream;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChatTextClient implements ChatClient {
    private final BinaryStream connection;

    private Chatroom subscribedChatroom;
    private User user;

    public ChatTextClient(String host, int port, String user, String password) throws IOException, InterruptedException, ValidationError {
        Socket socket = new Socket(host, port);
        DataStream stream = new DataStream(socket);
        stream.setUUID(Integer.toString(new Random().nextInt()));
        stream.setAPIVersion(V1Dispatcher.VERSION_ID);
        connection = stream;

        System.out.println("Connected to " + socket);

        InMemoryChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        InMemoryUserRepository userRepo = new InMemoryUserRepository();

        long userId = ChatClientUtilities.initialConnect(connection, user, password);
        this.user = new User(userId, user, userRepo);

        userRepo.addUser(this.user);

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatTextInput(this));
        pool.submit(new ChatClientDispatcher(this, connection, chatroomRepo, userRepo));
    }

    @Override
    public void onChatroom(Chatroom chatroom) throws IOException {
        System.out.println("New chatroom: " + chatroom.getName() + " by " + chatroom.getOwner().getHandle());

        // Subscribe to the first one!
        if (chatroom.getName().equalsIgnoreCase("Global")) {
            connection.sendMessage(new JoinChatroomMessage(user.getId(), chatroom.getId(), 0, 0), true);
            subscribedChatroom = chatroom;
        }
    }

    @Override
    public void onMessage(ChatMessage message) {
        System.out.println(message);
    }

    public void sendMessage(String message) throws IOException {
        connection.sendMessage(new SubmitMessageMessage(user.getId(), subscribedChatroom.getId(), message), true);
    }

    @Override
    public void onJoinedChatroom(Chatroom chat, User user) {
        System.out.println(user + " has joined " + chat);
    }

    @Override
    public void onLeftChatroom(Chatroom chatroom, User user) {
        System.out.println(user + " has left " + chatroom);
    }

    public static void main(String[] args) throws IOException, InterruptedException, ValidationError {
        int port = Integer.parseInt(args[1]);
        new ChatTextClient(args[0], port, args[2], args[3]);
    }
}
