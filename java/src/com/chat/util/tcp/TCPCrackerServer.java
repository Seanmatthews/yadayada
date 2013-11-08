package com.chat.util.tcp;

import com.chat.select.ClientSocket;
import com.chat.select.EventService;
import com.chat.select.SocketListener;
import com.chat.util.ByteCracker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class TCPCrackerServer implements SocketListener {
    private final ByteCracker cracker;
    private final TCPCrackerClientListener listener;
    private final Map<ClientSocket, TCPCrackerClient> socketClientMap = new HashMap<>();

    public TCPCrackerServer(EventService eventService, int port, ByteCracker cracker, TCPCrackerClientListener listener) throws IOException {
        this.cracker = cracker;
        this.listener = listener;

        eventService.createServerSocket(this, port);
    }

    @Override
    public void onConnect(ClientSocket clientSocket) {
        TCPCrackerClient client = new TCPCrackerClient(clientSocket.toString(), cracker, listener, clientSocket);
        socketClientMap.put(clientSocket, client);

        listener.onConnect(client);
    }

    @Override
    public void onReadAvailable(ClientSocket clientSocket) {
        TCPCrackerClient client = socketClientMap.get(clientSocket);

        if (client != null) {
            client.onReadAvailable();
        }
    }

    @Override
    public void onWriteAvailable(ClientSocket clientSocket) {
        TCPCrackerClient client = socketClientMap.get(clientSocket);

        if (client != null) {
            client.onWriteAvailable();
        }
    }
}
