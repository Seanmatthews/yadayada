package com.chat.select.impl;

import com.chat.select.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 9:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerSocketImpl implements ServerSocket, EventHandler {
    private final Logger log = LogManager.getLogger();

    private final ServerSocketChannel channel;
    private final SocketListener listener;
    private final EventService eventService;

    public ServerSocketImpl(EventService eventService, ServerSocketChannel channel, SocketListener listener) throws IOException {
        this.eventService = eventService;
        this.channel = channel;
        this.listener = listener;

        eventService.register(channel, this);
    }

    public void close() throws IOException {
        eventService.free(channel);
        channel.close();
    }

    @Override
    public void onAccept(ClientSocket clientSocket) {
        listener.onConnect(clientSocket);
    }

    @Override
    public void enableAccept(boolean accept) {
        eventService.enableAccept(channel, accept);
    }

    @Override
    public void onAccept() {
        try {
            SocketChannel acceptedChannel = channel.accept();
            ClientSocket clientSocket = new ClientSocketImpl(eventService, acceptedChannel, listener);
            clientSocket.enableRead(true);
        } catch (IOException e) {
            log.error("Error accepting client", e);
        }
    }

    @Override
    public void onConnect() {
        throw new RuntimeException("ServerSocket unable to connect");
    }

    @Override
    public void onRead() {
        throw new RuntimeException("ServerSocket unable to read");
    }

    @Override
    public void onWrite() {
        throw new RuntimeException("ServerSocket unable to write");
    }
}
