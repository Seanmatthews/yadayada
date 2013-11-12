package com.chat.select;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 9:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface EventService {
    void run();
    void runOnce();

    void addThreadedEvent(Runnable runnable);

    ServerSocketChannel createServerSocket() throws IOException;
    SocketChannel createClientSocket() throws IOException;

    void register(SelectableChannel channel, SelectHandler handler) throws IOException;
    void enableAccept(SelectableChannel channel, boolean val);
    void enableConnect(SocketChannel channel, boolean val);
    void enableWrite(SelectableChannel channel, boolean val);
    void enableRead(SelectableChannel channel, boolean val);
    void free(SelectableChannel channel);

    void wakeup();
}
