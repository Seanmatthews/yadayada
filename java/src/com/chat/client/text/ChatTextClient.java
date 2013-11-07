package com.chat.client.text;

import com.chat.*;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientConnection;
import com.chat.client.ChatClientDispatcher;
import com.chat.client.ChatClientUtilities;
import com.chat.msgs.V1Dispatcher;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.*;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;
import com.chat.select.EventService;
import com.chat.select.impl.EventServiceImpl;

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
    private final ChatClientConnection connection;
    private final InMemoryUserRepository userRepo;
    private final String userName;

    private Chatroom subscribedChatroom;
    private User user;

    public ChatTextClient(String host, int port, String user, String password) throws IOException, InterruptedException, ValidationError {
        EventService eventService = new EventServiceImpl();

        ChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        userRepo = new InMemoryUserRepository();
        this.userName = user;
        ChatClientDispatcher dispatcher = new ChatClientDispatcher(this, chatroomRepo, userRepo);

        connection = new ChatClientConnection("CLIENT", eventService, host, port, dispatcher, user, password);

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatTextInput(this));

        eventService.run();
    }

    @Override
    public void onLoginAccept(long userId) {
        user = new User(userId, userName, userRepo);
        userRepo.addUser(user);

        connection.sendMessage(new SearchChatroomsMessage(0, 0));
    }

    @Override
    public void onChatroom(Chatroom chatroom) throws IOException {
        System.out.println("New chatroom: " + chatroom.getName() + " by " + chatroom.getOwner().getHandle());

        // Subscribe to the first one!
        if (chatroom.getName().equalsIgnoreCase("Global")) {
            connection.sendMessage(new JoinChatroomMessage(user.getId(), chatroom.getId(), 0, 0));
            subscribedChatroom = chatroom;
        }
    }

    @Override
    public void onMessage(ChatMessage message) {
        System.out.println(message);
    }

    public void sendMessage(String message) throws IOException {
        connection.sendMessage(new SubmitMessageMessage(user.getId(), subscribedChatroom.getId(), message));
    }

    @Override
    public void onJoinedChatroom(Chatroom chat, User user) {
        System.out.println(user + " has joined " + chat);
    }

    @Override
    public void onLeftChatroom(Chatroom chatroom, User user) {
        System.out.println(user + " has left " + chatroom);
    }

    @Override
    public void onJoinedChatroomReject(String reason) {
        System.err.println("Error entering chatroom: " + reason);
    }

    public static void main(String[] args) throws IOException, InterruptedException, ValidationError {
        int port = Integer.parseInt(args[1]);
        new ChatTextClient(args[0], port, args[2], args[3]);
    }
}
