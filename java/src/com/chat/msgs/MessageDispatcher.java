package com.chat.msgs;

import com.chat.ClientConnection;
import com.chat.util.buffer.ReadBuffer;

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
    void onMessage(ClientConnection connection, ReadBuffer buffer) throws IOException, ValidationError, ExecutionException, InterruptedException, RuntimeException;
}
