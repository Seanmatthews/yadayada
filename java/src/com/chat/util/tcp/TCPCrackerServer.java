package com.chat.util.tcp;

import com.chat.select.*;
import com.chat.select.impl.ClientSocketImpl;
import com.chat.select.impl.ServerSocketImpl;
import com.chat.util.ByteCracker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class TCPCrackerServer {
    public TCPCrackerServer(EventService eventService, int port, final ByteCracker cracker, final TCPCrackerClientFactory factory) throws IOException {
        ServerSocketChannel channel = eventService.createServerSocket();
        channel.bind(new InetSocketAddress(port));

        ServerSocket serverSocket = new ServerSocketImpl(eventService, channel, new ClientSocketFactory() {
            @Override
            public ClientSocket createClickSocket(EventService eventService, SocketChannel channel) {
                try {
                    ClientSocket socket = new ClientSocketImpl(eventService, channel);
                    TCPCrackerClient client = factory.createClient(cracker, socket);
                    socket.setListener(client);
                    return socket;
                } catch (IOException e) {
                    return null;
                }
            }
        });

        serverSocket.enableAccept(true);
    }
}
