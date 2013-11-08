package com.chat.select;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 9:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ServerSocket {
    void close() throws IOException;
    SocketListener getListener();
    void onAccept(ClientSocket socket) throws IOException;
}
