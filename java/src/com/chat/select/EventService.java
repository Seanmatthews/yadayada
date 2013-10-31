package com.chat.select;

import java.io.IOException;
import java.nio.channels.SelectionKey;
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
    ServerSocket createServerSocket(SocketListener listener, int port) throws IOException;
    void run();

    SelectionKey register(ServerSocket socket, ServerSocketChannel channel) throws IOException;
    SelectionKey register(ClientSocket socket, SocketChannel clientChannel) throws IOException;

    void enableAccept(SelectionKey key, boolean val);
    void enableWrite(SelectionKey key, boolean val);
    void enableRead(SelectionKey key, boolean val);
    void free(SelectionKey key);
}
