package com.chat.select.impl;

import com.chat.select.ClientSocket;
import com.chat.select.EventService;
import com.chat.select.ServerSocket;
import com.chat.select.SocketListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 9:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventServiceImpl implements EventService {
    private final Selector selector;

    public EventServiceImpl(Selector selector) {
        this.selector = selector;
    }

    @Override
    public ServerSocket createServerSocket(SocketListener listener, int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);

        return new ServerSocketImpl(this, serverChannel, listener, port);
    }

    @Override
    public SelectionKey register(ServerSocket socket, ServerSocketChannel serverChannel) throws IOException {
        return serverChannel.register(selector, SelectionKey.OP_ACCEPT, socket);
    }

    @Override
    public SelectionKey register(ClientSocket socket, SocketChannel clientChannel) throws IOException {
        return clientChannel.register(selector, SelectionKey.OP_READ, socket);
    }

    @Override
    public void enableAccept(SelectionKey key, boolean val) {
        changeOp(key, val, SelectionKey.OP_ACCEPT);
        selector.wakeup();
    }

    @Override
    public void enableWrite(SelectionKey key, boolean val) {
        changeOp(key, val, SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    @Override
    public void enableRead(SelectionKey key, boolean val) {
        changeOp(key, val, SelectionKey.OP_READ);
        selector.wakeup();
    }

    @Override
    public void free(SelectionKey key) {
        key.cancel();
    }

    private void changeOp(SelectionKey key, boolean val, int newOp) {
        boolean isEnabled = (key.interestOps() & newOp) == 0;
        if (val) {
            if (isEnabled) {
                key.interestOps(key.interestOps() | newOp);
            }
        }
        else {
            if (!isEnabled) {
                key.interestOps(key.interestOps() & ~newOp);
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            int select = 0;
            try {
                select = selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (select > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while(iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    try {
                        processKey(key);
                    } catch(CancelledKeyException ignored) {

                    } finally {
                        iterator.remove();
                    }

                }
            }
        }
    }

    private void processKey(SelectionKey key) throws CancelledKeyException {
        // accepting a client socket
        if (key.isValid() && key.isAcceptable()) {
            handleAccept(key);
        }

        // writing to a client socket
        if (key.isValid() && key.isWritable()) {
            handleWrite(key);
        }

        // reading a client socket
        if (key.isValid() && key.isReadable()) {
            handleRead(key);
        }
    }

    private void handleAccept(SelectionKey key) {
        ServerSocket serverSocket = (ServerSocket) key.attachment();

        try {
            serverSocket.accept();
        }
        catch(IOException e) {
            System.err.println("Error accepting socket");
        }
    }

    private void handleWrite(SelectionKey key) {
        ClientSocket clientSocket = (ClientSocket) key.attachment();
        clientSocket.enableWrite(false);
        clientSocket.onWriteAvailable();
    }

    private void handleRead(SelectionKey key) {
        ClientSocket clientSocket = (ClientSocket) key.attachment();
        clientSocket.onReadAvailable();
    }
}
