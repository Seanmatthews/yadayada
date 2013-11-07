package com.chat.client.load;

import com.chat.ChatMessage;
import com.chat.Chatroom;
import com.chat.ChatroomRepository;
import com.chat.User;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientConnection;
import com.chat.client.ChatClientDispatcher;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.JoinChatroomMessage;
import com.chat.msgs.v1.SearchChatroomsMessage;
import com.chat.msgs.v1.SubmitMessageMessage;
import com.chat.select.EventService;
import com.chat.select.impl.EventServiceImpl;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class LoadTesterClient implements ChatClient, Runnable {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final CountDownLatch latch;
    private ChatClientConnection connection;

    private Chatroom subscribedChatroom;
    private User user;
    public volatile boolean alive;
    public AtomicInteger messagesRecv = new AtomicInteger(0);

    public LoadTesterClient(String host, int port, String user, String password, CountDownLatch latch) throws IOException, InterruptedException, ValidationError {
        this.host = host;
        this.port = port;
        this.username = user;
        this.password = password;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            EventService eventService = new EventServiceImpl();

            ChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
            InMemoryUserRepository userRepo = new InMemoryUserRepository();
            ChatClientDispatcher dispatcher = new ChatClientDispatcher(this, chatroomRepo, userRepo);

            //connection = new ChatClientConnection("CLIENT", eventService, host, port, dispatcher);

            //long userId = ChatClientUtilities.initialConnect(connection, userName, password);
            //user = new User(userId, userName, userRepo);
            //userRepo.addUser(user);

            connection.sendMessage(new SearchChatroomsMessage(0, 0));
            Chatroom global = new Chatroom(1, "Global", user, chatroomRepo);
            connection.sendMessage(new JoinChatroomMessage(user.getId(), global.getId(), 0, 0));
            subscribedChatroom = global;

            alive = true;
            latch.countDown();
        }
        catch(Exception e) {
            alive = false;
            System.err.println("Crapping out! " + e.getMessage());
            e.printStackTrace();
            latch.countDown();
        }
    }

    @Override
    public void onChatroom(Chatroom chatroom) throws IOException {

    }

    @Override
    public void onMessage(ChatMessage message) {
        messagesRecv.incrementAndGet();
    }

    public void sendMessage(String message) throws IOException {
        connection.sendMessage(new SubmitMessageMessage(user.getId(), subscribedChatroom.getId(), message));
    }

    @Override
    public void onJoinedChatroom(Chatroom chat, User user) {

    }

    @Override
    public void onLeftChatroom(Chatroom chatroom, User user) {

    }

    @Override
    public void onJoinedChatroomReject(String reason) {
        System.err.println("Error entering chatroom: " + reason);
    }

    @Override
    public void onLoginAccept(long userId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
