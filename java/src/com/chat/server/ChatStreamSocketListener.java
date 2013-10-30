package com.chat.server;

import com.chat.*;
import com.chat.impl.SynchronousSocketChannel;
import com.chat.msgs.MessageDispatcherFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatStreamSocketListener {
    private final ExecutorService execService = Executors.newCachedThreadPool();
    private final MessageDispatcherFactory factory;

    public ChatStreamSocketListener(int port, UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) throws IOException {
        factory = new MessageDispatcherFactory(new ChatServerImpl(userRepo, chatroomRepo, messageRepo), userRepo, chatroomRepo);
        listen(port);
    }

    private void listen(int port) throws IOException {
        //ServerSocket serverSocket = new ServerSocket(port);

        ServerSocketChannel channel = ServerSocketChannel.open();
        ServerSocketChannel bindable = channel.bind(new InetSocketAddress(port));

        System.out.println("Listening on: " + port);

        while (true) {
            SocketChannel clientChannel = bindable.accept();
            System.out.println("BinaryStream from: " + clientChannel);
            execService.submit(new ConnectionReceiver(factory, new SynchronousSocketChannel(clientChannel)));
        }
    }
}
