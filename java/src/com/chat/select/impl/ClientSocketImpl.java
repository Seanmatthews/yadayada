package com.chat.select.impl;

import com.chat.select.*;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 9:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientSocketImpl implements ClientSocket, SelectHandler {
    private final Logger log = LogManager.getLogger();
    private final SocketChannel channel;
    private final EventService eventService;

    private ClientSocketListener listener;

    public ClientSocketImpl(EventService eventService, SocketChannel clientChannel, ClientSocketListener listener) throws IOException {
        this.eventService = eventService;
        this.channel = clientChannel;
        this.listener = listener;

        eventService.register(clientChannel, this);
    }

    public ClientSocketImpl(EventService eventService, SocketChannel clientChannel) throws IOException {
        this.eventService = eventService;
        this.channel = clientChannel;

        eventService.register(clientChannel, this);
    }

    public void setListener(ClientSocketListener listener) {
        this.listener = listener;
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
        try {
            if (channel.finishConnect()) {
                log.info("Connected {}", channel);

                enableConnect(false);
                enableRead(true);

                listener.onConnect(this);
            }
            else {
                log.error("Could not finish connect {}", channel);
                close();
            }
        } catch (IOException e) {
            log.error("Error connecting " + channel, e);
            close();
        }
    }

    @Override
    public void onWrite() {
        enableWrite(false);
        listener.onWriteAvailable(this);
    }

    @Override
    public void onRead() {
        listener.onReadAvailable(this);
    }

    public void onAccept() {
        throw new RuntimeException("Cannot accept on a client socket");
    }

    @Override
    public int read(ReadBuffer buffer) {
        if (!isConnected())
            return 0;

        try {
            int read = channel.read(buffer.getRawBuffer());

            if (read == -1) {
                log.debug("EOF. Hanging up {}", channel);
                close();
                return 0;
            }

            return read;
        } catch (IOException e) {
            log.error("Error reading " + channel, e);
            close();

            return 0;
        }
    }

    @Override
    public void write(ReadWriteBuffer output) {
        if (!isConnected())
            return;

        ByteBuffer outBuffer = output.getRawBuffer();

        try {
            channel.write(outBuffer);

            if (outBuffer.hasRemaining()) {
                listener.onWriteUnavailable(this);
            }
        } catch (IOException e) {
            //log.error("Error writing " + channel, e);
            close();
        }
    }

    @Override
    public void close() {
        log.debug("Closing channel {}", channel);

        eventService.free(channel);

        try {
            channel.close();
        } catch (IOException e) {
            log.debug("Error closing channel " + channel, e);
        }

        listener.onDisconnect(this);
    }

    @Override
    public boolean isConnected() {
        return channel.isConnected();
    }

    @Override
    public void connect(String host, int port) throws IOException {
        log.debug("Connecting {}:{}", host, port);

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
