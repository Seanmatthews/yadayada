package com.chat.impl;

import com.chat.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatStreamServer {
    private final ExecutorService execService = Executors.newCachedThreadPool();

    private final UserRepository userRepo;
    private final ChatroomRepository chatroomRepo;
    private final MessageRepository messageRepo;

    private final ChatServer server;

    public ChatStreamServer(int port,
                            UserRepository userRepo,
                            ChatroomRepository chatroomRepo,
                            MessageRepository messageRepo) throws IOException {
        // not really an Impl - what's that pattern?
        this.server = new ChatServerImpl(userRepo, chatroomRepo, messageRepo);
        this.userRepo = userRepo;
        this.chatroomRepo = chatroomRepo;
        this.messageRepo = messageRepo;

        listen(port);
    }

    private void listen(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Listening on: " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Connection from: " + socket);

            Connection connection = new SocketConnection(socket);
            execService.submit(new ChatServerListener(server, connection, userRepo, chatroomRepo));
        }
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);

        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        User admin = userRepo.registerUser("admin", "admin");

        InMemoryChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        chatroomRepo.createChatroom(admin, "Global");

        MessageRepository repo = new InMemoryMessageRepository();

        new ChatStreamServer(port, userRepo, chatroomRepo, repo);
    }
}
