package com.chat.server;

import com.chat.ChatroomRepository;
import com.chat.MessageRepository;
import com.chat.UserRepository;
import com.chat.msgs.MessageDispatcher;
import com.chat.msgs.V1Dispatcher;
import com.chat.select.EventService;
import com.chat.util.TwoByteLengthMessageCracker;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;
import com.chat.util.tcp.TCPCrackerClient;
import com.chat.util.tcp.TCPCrackerClientListener;
import com.chat.util.tcp.TCPCrackerServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerListener implements TCPCrackerClientListener {
    private final Logger log = LogManager.getLogger();

    private final MessageDispatcher dispatcher;

    private final Map<TCPCrackerClient, ServerClientConnection> tcpConnectionMap = new HashMap<>();

    public ServerListener(EventService eventService, int port, UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) throws IOException {
        ChatServer server = new ChatServerImpl(userRepo, chatroomRepo, messageRepo);
        dispatcher = new V1Dispatcher(server, userRepo, chatroomRepo);

        new TCPCrackerServer(eventService, port, new TwoByteLengthMessageCracker(), this);
        log.info("Listening on {}", port);

        eventService.run();
    }

    @Override
    public void onConnect(TCPCrackerClient client) {
        try {
            ServerClientConnection connection = new ServerClientConnection(client, dispatcher);
            tcpConnectionMap.put(client, connection);
        } catch (IOException e) {
            log.error("Error connecting " + client.toString(), e);
        }
    }

    @Override
    public void onCracked(TCPCrackerClient client, ReadBuffer slice) {
        ServerClientConnection connection = tcpConnectionMap.get(client);

        if (connection != null) {
            connection.onCracked(slice);
        }
    }

    @Override
    public void onWriteAvailable(TCPCrackerClient client, ReadWriteBuffer writeBuffer) {
        ServerClientConnection connection = tcpConnectionMap.get(client);

        if (connection != null) {
            connection.onWriteAvailable(writeBuffer);
        }
    }
}
