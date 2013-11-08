package com.chat.select.impl;

import com.chat.select.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private final Logger log = LogManager.getLogger();

    private final Selector selector;

    public EventServiceImpl() throws IOException {
        this(Selector.open());
    }

    protected EventServiceImpl(Selector selector) {
        this.selector = selector;
    }

    @Override
    public ServerSocket createServerSocket(SocketListener listener, int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);

        return new ServerSocketImpl(this, serverChannel, listener);
    }

    @Override
    public ClientSocket createClientSocket(SocketListener listener) throws IOException {
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);

        ClientSocket socket = new ClientSocketImpl(this, clientChannel, listener);
        socket.enableConnect(true);

        return socket;
    }


    @Override
    public void register(SelectableChannel channel, Object socket) throws IOException {
        channel.register(selector, 0, socket);
    }

    @Override
    public void free(SelectableChannel channel) {
        SelectionKey key = channel.keyFor(selector);

        if (key != null)
            key.cancel();
    }

    @Override
    public void enableAccept(SelectableChannel channel, boolean val) {
        SelectionKey key = channel.keyFor(selector);

        if (key != null) {
            changeOp(key, val, SelectionKey.OP_ACCEPT);
            selector.wakeup();
        }
    }

    @Override
    public void enableConnect(SocketChannel channel, boolean val) {
        SelectionKey key = channel.keyFor(selector);

        if (key != null) {
            changeOp(key, val, SelectionKey.OP_CONNECT);
            selector.wakeup();
        }
    }

    @Override
    public void enableWrite(SelectableChannel channel, boolean val) {
        SelectionKey key = channel.keyFor(selector);

        if (key != null) {
            changeOp(key, val, SelectionKey.OP_WRITE);
            selector.wakeup();
        }
    }

    @Override
    public void enableRead(SelectableChannel channel, boolean val) {
        SelectionKey key = channel.keyFor(selector);

        if (key != null) {
            changeOp(key, val, SelectionKey.OP_READ);
            selector.wakeup();
        }
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

        // connecting to a server socket
        if (key.isValid() && key.isConnectable()) {
            handleConnect(key);
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
            SocketChannel clientChannel = ((ServerSocketChannel)key.channel()).accept();
            clientChannel.configureBlocking(false);

            SocketListener listener = ((ServerSocket) key.attachment()).getListener();
            ClientSocketImpl clientSocket = new ClientSocketImpl(this, clientChannel, listener);
            clientSocket.enableRead(true);
            serverSocket.onAccept(clientSocket);
        }
        catch(IOException e) {
            log.error("Error accepting socket");
        }
    }

    private void handleConnect(SelectionKey key) {
        ClientSocket clientSocket = (ClientSocket) key.attachment();

        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (channel.finishConnect()) {
                clientSocket.enableConnect(false);
                clientSocket.enableRead(true);
                clientSocket.onConnect();
            }
        } catch (IOException e) {
            clientSocket.close();
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
