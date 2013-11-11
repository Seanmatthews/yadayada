package com.chat.select;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 12:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ClientSocketListener {
    void onConnect(ClientSocket clientSocket);
    void onDisconnect(ClientSocket clientSocket);

    void onReadAvailable(ClientSocket clientSocket);
    void onWriteAvailable(ClientSocket clientSocket);
    void onWriteUnavailable(ClientSocket clientSocket);
}
