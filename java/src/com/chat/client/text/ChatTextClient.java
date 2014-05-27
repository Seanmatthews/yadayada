package com.chat.client.text;

import com.chat.*;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientConnection;
import com.chat.client.ChatClientDispatcher;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.*;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;
import com.chat.select.EventService;
import com.chat.select.impl.EventServiceImpl;

import java.io.IOException;
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

    private final String username;
    private final String password;
    private final String uuid;
    private final long phoneNumber;

    private Chatroom subscribedChatroom;
    private User user;

    public ChatTextClient(String host, int port, String user, String pass, Long phone, String UUID) throws IOException, InterruptedException, ValidationError {
        EventService eventService = new EventServiceImpl();

        ChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        userRepo = new InMemoryUserRepository();
        username = user;
        password = pass;
        phoneNumber = phone;
        uuid = UUID;
        ChatClientDispatcher dispatcher = new ChatClientDispatcher(this, chatroomRepo, userRepo);

        connection = new ChatClientConnection("CLIENT", eventService, host, port, dispatcher);

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatTextInput(this));

        eventService.run();
    }

    @Override
    public void onConnectAccept(int apiVersion, long globalChatId) {
        connection.sendMessage(new QuickLoginMessage(username, username, 12155551212L));
    }

    @Override
    public void onConnectReject(String reason) {
        System.err.println(reason);
        System.exit(0);
    }

    @Override
    public void onLoginAccept(long userId) {
        user = new User(userId, uuid, username, phoneNumber, "", userRepo);
        userRepo.addUser(user);

        connection.sendMessage(new SearchChatroomsMessage(0L, 0L, (byte)0, 0L));
    }

    @Override
    public void onLoginReject(String reason) {
        System.err.println(reason);
        System.exit(0);
    }

    @Override
    public void onRegisterAccept(long userId) {
        connection.sendMessage(new LoginMessage(username, password));
    }

    @Override
    public void onRegisterReject(String reason) {
        connection.sendMessage(new LoginMessage(username, password));
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
        long phone = Long.parseLong(args[4]);
        new ChatTextClient(args[0], port, args[2], args[3], phone, args[5]);
    }
}
