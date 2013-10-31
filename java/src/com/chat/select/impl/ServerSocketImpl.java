package com.chat.select.impl;

import com.chat.select.ClientSocket;
import com.chat.select.EventService;
import com.chat.select.ServerSocket;
import com.chat.select.SocketListener;

import java.io.IOException;
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
    private final ServerSocketChannel channel;
    private final SocketListener listener;
    private final EventService eventService;

    public ServerSocketImpl(EventService eventService, ServerSocketChannel channel, SocketListener listener) throws IOException {
        this.eventService = eventService;
        this.channel = channel;
        this.listener = listener;
        eventService.register(channel, this);
        eventService.enableAccept(channel, true);
    }

    public void close() throws IOException {
        eventService.free(channel);
        channel.close();
    }

    @Override
    public ClientSocket accept() throws IOException {
        SocketChannel channel = this.channel.accept();
        channel.configureBlocking(false);
        ClientSocketImpl clientSocket = new ClientSocketImpl(eventService, channel, listener);
        listener.onConnect(clientSocket);
        return clientSocket;
    }
}
