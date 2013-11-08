package com.chat.select;

import com.chat.select.impl.ClientSocketImpl;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 9:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ServerSocket {
    void onAccept(ClientSocket clientSocket);
    void enableAccept(boolean accept);
    void close() throws IOException;
}
