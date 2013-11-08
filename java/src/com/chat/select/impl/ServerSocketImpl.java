package com.chat.select.impl;

import com.chat.select.*;

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
    public SocketListener getListener() {
        return listener;
    }

    @Override
    public void onAccept(ClientSocket clientSocket) throws IOException {
        listener.onConnect(clientSocket);
    }
}
