package com.chat.select.impl;

import com.chat.select.ClientSocket;
import com.chat.select.EventService;
import com.chat.select.ServerSocket;
import com.chat.select.SocketListener;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 9:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerSocketImpl implements ServerSocket {
    private final ServerSocketChannel serverChannel;
    private final SocketListener listener;
    private final EventService eventService;

    private final SelectionKey key;
    private final int port;

    public ServerSocketImpl(EventService eventService, ServerSocketChannel channel, SocketListener listener, int port) throws IOException {
        this.eventService = eventService;
        this.serverChannel = channel;
        this.listener = listener;
        this.port = port;
        this.key = eventService.register(this, channel);
        eventService.enableAccept(key, true);
    }

    @Override
    public ServerSocket open() throws IOException {
        if (serverChannel.isOpen())
            throw new IOException(serverChannel + " is already open");

        return eventService.createServerSocket(listener, port);
    }

    public void close() throws IOException {
        if (serverChannel.isOpen()) {
            serverChannel.close();
            eventService.free(key);
        }
    }

    @Override
    public ClientSocket accept() throws IOException {
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        ClientSocketImpl clientSocket = new ClientSocketImpl(eventService, channel, listener);
        listener.onConnect(clientSocket);
        return clientSocket;
    }
}
