package com.chat.server;

import com.chat.ClientConnection;
import com.chat.msgs.Message;
import com.chat.msgs.MessageDispatcher;
import com.chat.msgs.ValidationError;
import com.chat.select.EventService;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;
import com.chat.util.tcp.TCPCrackerClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerClientConnection implements ClientConnection {
    private final Logger log = LogManager.getLogger();
    private final MessageDispatcher dispatcher;
    private final TCPCrackerClient socket;

    public ServerClientConnection(TCPCrackerClient socket, MessageDispatcher dispatcher) throws IOException {
        this.socket = socket;
        this.dispatcher = dispatcher;
    }

    public void onCracked(ReadBuffer slice) throws InterruptedException, ExecutionException, ValidationError, IOException {
        // skip size
        slice.advance(2);
        dispatcher.onMessage(this, slice);
    }

    @Override
    public void close() {
        socket.close();
    }

    @Override
    public void sendMessage(final Message message) throws IOException {
        ReadWriteBuffer output = socket.getWriteBuffer();

        // write to buffer
        message.write(output);
        socket.write();

        // Message got queued up
        if (output.position() != 0) {
            throw new IOException("Too many messages in the queue to send. Terminating.");
        }
    }

    @Override
    public String toString() {
        return socket.toString();
    }
}
