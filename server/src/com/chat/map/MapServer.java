package com.chat.map;

import com.chat.BinaryStream;
import com.chat.Chatroom;
import com.chat.Message;
import com.chat.User;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientDispatcher;
import com.chat.client.ChatClientUtilities;
import com.chat.impl.DataStream;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryMessageRepository;
import com.chat.impl.InMemoryUserRepository;
import com.chat.msgs.v1.ServerConnectionImpl;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/29/13
 * Time: 9:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class MapServer implements ChatClient {
    private final ServerConnectionImpl connection;
    private final User user;

    public MapServer(String host, int port, String username, String password) throws IOException {
        Socket socket = new Socket(host, port);
        BinaryStream dout = new DataStream(socket);

        System.out.println("Connected to " + socket);

        connection = new ServerConnectionImpl(dout);

        long userId = ChatClientUtilities.initialConnect(connection, username, password);
        user = new User(userId, username);

        InMemoryChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        userRepo.addUser(user);
        new InMemoryMessageRepository();

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatClientDispatcher(this, dout, chatroomRepo, userRepo));
    }

    public static void main(String[] args) throws IOException {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String username = args[2];
        String password = args[3];
        new MapServer(host, port, username, password);
    }

    @Override
    public void onChatroom(Chatroom chatroom) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onMessage(Message message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onJoinedChatroom(Chatroom chat, User user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLeftChatroom(Chatroom chatroom, User user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
