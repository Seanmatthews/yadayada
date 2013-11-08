package com.chat.client;

import com.chat.msgs.Message;
import com.chat.msgs.V1Dispatcher;
import com.chat.msgs.v1.ConnectMessage;
import com.chat.msgs.v1.LoginMessage;
import com.chat.msgs.v1.RegisterMessage;
import com.chat.select.ClientSocket;
import com.chat.select.EventService;
import com.chat.select.SocketListener;
import com.chat.util.TwoByteLengthMessageCracker;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;
import com.chat.util.tcp.TCPCrackerClient;
import com.chat.util.tcp.TCPCrackerClientListener;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatClientConnection implements TCPCrackerClientListener, SocketListener {
    private final ChatClientDispatcher dispatcher;
    private final TCPCrackerClient socket;
    private final String name;
    private final String username;
    private final String password;

    public ChatClientConnection(String name,
                                EventService eventService,
                                String host,
                                int port,
                                ChatClientDispatcher dispatcher,
                                String username,
                                String password) throws IOException {
        this.name = name;
        this.socket = new TCPCrackerClient(name, new TwoByteLengthMessageCracker(), this, eventService.createClientSocket(this));
        this.socket.connect(host, port);
        this.dispatcher = dispatcher;
        this.username = username;
        this.password = password;
    }

    @Override
    public void onConnect(TCPCrackerClient client) {
        ReadWriteBuffer writeBuffer = socket.getWriteBuffer();

        ConnectMessage message = new ConnectMessage(V1Dispatcher.VERSION_ID, name);
        message.write(writeBuffer);

        RegisterMessage register = new RegisterMessage(username, password, username, username);
        register.write(writeBuffer);

        LoginMessage login = new LoginMessage(username, password);
        login.write(writeBuffer);

        socket.write();
    }

    @Override
    public void onCracked(TCPCrackerClient client, ReadBuffer slice) {
        // skip size
        slice.advance(2);

        dispatcher.onMessage(slice);
    }

    @Override
    public void onWriteAvailable(TCPCrackerClient client, ReadWriteBuffer writeBuffer) {
    }

    public void sendMessage(Message message) {
        message.write(socket.getWriteBuffer());
        socket.write();
    }

    @Override
    public void onConnect(ClientSocket clientSocket) {
        socket.onConnect();
    }

    @Override
    public void onReadAvailable(ClientSocket clientSocket) {
        socket.onReadAvailable();
    }

    @Override
    public void onWriteAvailable(ClientSocket clientSocket) {
        socket.onWriteAvailable();
    }
}
