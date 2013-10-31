package com.chat.msgs;

import com.chat.msgs.v1.ClientConnection;
import com.chat.server.ChatServer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/29/13
 * Time: 9:14 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MessageDispatcher {
    void run();
    void runOnce(ClientConnection connection) throws IOException, ValidationError, ExecutionException, InterruptedException;
}
