package com.chat.client;

import com.chat.msgs.Message;
import com.chat.msgs.V1_4_0Dispatcher;
import com.chat.msgs.v1_4_0.ConnectMessage;
import com.chat.select.ClientSocket;
import com.chat.select.EventService;
import com.chat.select.impl.ClientSocketImpl;
import com.chat.util.TwoByteLengthMessageCracker;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.tcp.TCPCrackerClient;
import com.chat.util.tcp.TCPCrackerClientListener;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatClientConnection implements TCPCrackerClientListener {
    private final ChatClientDispatcher dispatcher;
    private final TCPCrackerClient crackerClient;
    private final String name;
    private boolean connected = false;

    public ChatClientConnection(String name,
                                EventService eventService,
                                String host,
                                int port,
                                ChatClientDispatcher dispatcher) throws IOException {
        SocketChannel channel = eventService.createClientSocket();
        ClientSocket socket = new ClientSocketImpl(eventService, channel);
        socket.enableConnect(true);

        this.name = name;

        crackerClient = new TCPCrackerClient(new TwoByteLengthMessageCracker(), socket, this);
        socket.setListener(crackerClient);

        this.crackerClient.connect(host, port);
        this.dispatcher = dispatcher;
    }

    @Override
    public void onConnect(TCPCrackerClient client) {
        connected = true;
        sendMessage(new ConnectMessage(V1_4_0Dispatcher.VERSION_ID, name, ""));
    }

    @Override
    public void onCracked(TCPCrackerClient client, ReadBuffer slice) {
        // skip size
        slice.advance(2);

        dispatcher.onMessage(slice);
    }

    @Override
    public void onDisconnect(TCPCrackerClient client) {
        connected = false;
    }

    public void sendMessage(Message message) {
        message.write(crackerClient.getWriteBuffer());
        crackerClient.write();
    }

    public boolean isConnected() {
        return connected;
    }
}
