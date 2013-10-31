package com.chat.select.impl;

import com.chat.select.ClientSocket;
import com.chat.select.EventService;
import com.chat.select.SocketListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 9:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientSocketImpl implements ClientSocket {
    private final SelectionKey key;
    private final SocketListener listener;
    private final SocketChannel channel;
    private final EventService eventService;

    public ClientSocketImpl(EventService eventService, SocketChannel clientChannel, SocketListener listener) throws IOException {
        this.eventService = eventService;
        this.channel = clientChannel;
        this.listener = listener;
        this.key = eventService.register(this, clientChannel);
        eventService.enableRead(key, true);
    }

    @Override
    public void enableRead(boolean val) {
        eventService.enableRead(key, val);
    }

    @Override
    public void enableWrite(boolean val) {
        eventService.enableWrite(key, val);
    }

    @Override
    public void onWriteAvailable() {
        listener.onWriteAvailable(this);
    }

    @Override
    public void onReadAvailable() {
        listener.onReadAvailable(this);
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        return channel.read(buffer);
    }

    @Override
    public void write(ByteBuffer output) throws IOException {
        channel.write(output);
    }

    @Override
    public void close() throws IOException {
        channel.close();
        eventService.free(key);
    }
}
