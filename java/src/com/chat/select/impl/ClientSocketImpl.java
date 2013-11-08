package com.chat.select.impl;

import com.chat.select.ClientSocket;
import com.chat.select.SocketListener;
import com.chat.select.EventService;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 9:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientSocketImpl implements ClientSocket {
    private final SocketListener listener;
    private final SocketChannel channel;
    private final EventService eventService;

    public ClientSocketImpl(EventService eventService, SocketChannel clientChannel, SocketListener listener) throws IOException {
        this.eventService = eventService;
        this.channel = clientChannel;
        this.listener = listener;
        eventService.register(clientChannel, this);
    }

    @Override
    public void enableConnect(boolean val) {
        eventService.enableConnect(channel, val);
    }

    @Override
    public void enableRead(boolean val) {
        eventService.enableRead(channel, val);
    }

    @Override
    public void enableWrite(boolean val) {
        eventService.enableWrite(channel, val);
    }

    @Override
    public void onConnect() {
        listener.onConnect(this);
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
    public int read(ReadBuffer buffer) throws IOException {
        return channel.read(buffer.getRawBuffer());
    }

    @Override
    public void write(ReadWriteBuffer output) throws IOException {
        channel.write(output.getRawBuffer());
    }

    @Override
    public void close() {
        eventService.free(channel);

        try {
            channel.close();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void connect(String host, int port) throws IOException {
        channel.connect(new InetSocketAddress(host, port));
    }

    public String toString() {
        SocketAddress remoteSocketAddress = channel.socket().getRemoteSocketAddress();

        if (remoteSocketAddress == null) {
            return "Not Connected";
        }

        return remoteSocketAddress.toString();
    }
}
