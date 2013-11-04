package com.chat.client.load;

import com.chat.BinaryStream;
import com.chat.ChatMessage;
import com.chat.Chatroom;
import com.chat.User;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientDispatcher;
import com.chat.client.ChatClientUtilities;
import com.chat.impl.DataStream;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;
import com.chat.msgs.V1Dispatcher;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.JoinChatroomMessage;
import com.chat.msgs.v1.SubmitMessageMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

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
    private BinaryStream connection;

    private Chatroom subscribedChatroom;
    private User user;
    public volatile boolean alive;

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
            Socket socket = new Socket(host, port);
            DataStream stream = new DataStream(socket);
            stream.setUUID(Integer.toString(new Random().nextInt()));
            stream.setAPIVersion(V1Dispatcher.VERSION_ID);
            connection = stream;

            //System.out.println("Connected to " + socket);

            InMemoryChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
            InMemoryUserRepository userRepo = new InMemoryUserRepository();

            long userId = ChatClientUtilities.initialConnect(connection, username, password);
            user = new User(userId, username, userRepo);
            userRepo.addUser(user);

            Chatroom global = new Chatroom(1, "Global", user, chatroomRepo);
            connection.sendMessage(new JoinChatroomMessage(user.getId(), global.getId(), 0, 0), true);
            subscribedChatroom = global;

            alive = true;
            latch.countDown();

            new ChatClientDispatcher(this, connection, chatroomRepo, userRepo).run();
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
        //System.out.println(message);
    }

    public void sendMessage(String message) throws IOException {
        connection.sendMessage(new SubmitMessageMessage(user.getId(), subscribedChatroom.getId(), message), true);
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
}
