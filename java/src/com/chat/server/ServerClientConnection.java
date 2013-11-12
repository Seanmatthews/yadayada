package com.chat.server;

import com.chat.ClientConnection;
import com.chat.User;
import com.chat.msgs.Message;
import com.chat.msgs.MessageDispatcher;
import com.chat.msgs.ValidationError;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;
import com.chat.util.tcp.TCPCrackerClient;
import com.chat.util.tcp.TCPCrackerClientListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerClientConnection implements ClientConnection, TCPCrackerClientListener {
    private final Logger log = LogManager.getLogger();
    private final MessageDispatcher dispatcher;
    private final TCPCrackerClient socket;
    private final ChatServer server;
    private User user;
    private boolean connected;

    public ServerClientConnection(ChatServer server, TCPCrackerClient socket, MessageDispatcher dispatcher)  {
        this.server = server;
        this.socket = socket;
        this.dispatcher = dispatcher;
        this.connected = true;
    }

    public void close() {
        connected = false;
        socket.close();
    }

    @Override
    public void sendMessage(final Message message) {
        if (!connected)
            return;

        ReadWriteBuffer output = socket.getWriteBuffer();

        // write to buffer
        message.write(output);
        socket.write();
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return socket.toString();
    }

    @Override
    public void onConnect(TCPCrackerClient client) {
        // nothing
    }

    @Override
    public void onDisconnect(TCPCrackerClient client) {
        connected = false;
        server.disconnect(this);
    }

    @Override
    public void onCracked(TCPCrackerClient client, ReadBuffer slice) {
        try {
            slice.advance(2);
            dispatcher.onMessage(this, slice);
        } catch (ValidationError e) {
            log.info("Validation error " + this, e);
            close();
        } catch (RuntimeException e) {
            log.error("Unknown exception", e);
            close();
        }
    }
}
