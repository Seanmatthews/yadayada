package com.chat.server;

import com.chat.*;
import com.chat.impl.DataStream;
import com.chat.msgs.MessageDispatcherFactory;

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
public class StreamSocketListener {
    private final ExecutorService execService = Executors.newCachedThreadPool();
    private final MessageDispatcherFactory factory;

    public StreamSocketListener(int port, UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) throws IOException {
        factory = new MessageDispatcherFactory(new ChatServerImpl(userRepo, chatroomRepo, messageRepo), userRepo, chatroomRepo);
        listen(port);
    }

    private void listen(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("Listening on: " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("BinaryStream from: " + clientSocket);
            execService.submit(new ConnectionReceiver(factory, new DataStream(clientSocket)));
        }
    }
}
