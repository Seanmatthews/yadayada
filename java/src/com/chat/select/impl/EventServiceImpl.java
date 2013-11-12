package com.chat.select.impl;

import com.chat.select.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private final Queue<Runnable> threadEvents = new ConcurrentLinkedQueue<>();

    public EventServiceImpl() throws IOException {
        this(Selector.open());
    }

    protected EventServiceImpl(Selector selector) {
        this.selector = selector;
    }

    @Override
    public ServerSocketChannel createServerSocket() throws IOException {
        return ServerSocketChannel.open();
    }

    @Override
    public SocketChannel createClientSocket() throws IOException {
        return SocketChannel.open();
    }

    @Override
    public void register(SelectableChannel channel, SelectHandler handler) throws IOException {
        channel.configureBlocking(false);
        channel.register(selector, 0, handler);
    }

    @Override
    public void free(SelectableChannel channel) {
        SelectionKey key = channel.keyFor(selector);

        if (key != null)
            key.cancel();
    }

    @Override
    public void wakeup() {
        selector.wakeup();
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
        boolean isNotEnabled = (key.interestOps() & newOp) == 0;
        if (val) {
            if (isNotEnabled) {
                key.interestOps(key.interestOps() | newOp);
            }
        }
        else {
            if (!isNotEnabled) {
                key.interestOps(key.interestOps() & ~newOp);
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            runOnce();
        }
    }

    public void runOnce() {
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
                }

                iterator.remove();
            }
        }

        Runnable runnable;
        while((runnable = threadEvents.poll()) != null) {
            runnable.run();
        }
    }

    @Override
    public void addThreadedEvent(Runnable runnable) {
        threadEvents.add(runnable);
        selector.wakeup();
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
        SelectHandler handler = (SelectHandler) key.attachment();
        handler.onAccept();
    }

    private void handleConnect(SelectionKey key) {
        SelectHandler handler = (SelectHandler) key.attachment();
        handler.onConnect();
    }

    private void handleWrite(SelectionKey key) {
        SelectHandler handler = (SelectHandler) key.attachment();
        handler.onWrite();
    }

    private void handleRead(SelectionKey key) {
        SelectHandler handler = (SelectHandler) key.attachment();
        handler.onRead();
    }
}
