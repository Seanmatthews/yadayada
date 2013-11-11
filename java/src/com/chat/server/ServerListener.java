package com.chat.server;

import com.chat.ChatroomRepository;
import com.chat.MessageRepository;
import com.chat.UserRepository;
import com.chat.msgs.MessageDispatcher;
import com.chat.msgs.V1Dispatcher;
import com.chat.msgs.ValidationError;
import com.chat.select.ClientSocket;
import com.chat.select.EventService;
import com.chat.util.ByteCracker;
import com.chat.util.TwoByteLengthMessageCracker;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.tcp.TCPCrackerClient;
import com.chat.util.tcp.TCPCrackerClientFactory;
import com.chat.util.tcp.TCPCrackerClientListener;
import com.chat.util.tcp.TCPCrackerServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerListener {
    public ServerListener(EventService eventService, int port, UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) throws IOException {
        final ChatServer server = new ChatServerImpl(eventService, userRepo, chatroomRepo, messageRepo);
        final MessageDispatcher dispatcher = new V1Dispatcher(server, userRepo, chatroomRepo);
        final Logger log = LogManager.getLogger();

        new TCPCrackerServer(eventService, port, new TwoByteLengthMessageCracker(), new TCPCrackerClientFactory() {
            @Override
            public TCPCrackerClient createClient(ByteCracker cracker, ClientSocket socket) {
                TCPCrackerClient client = new TCPCrackerClient(cracker, socket);
                ServerClientConnection connection = new ServerClientConnection(server, client, dispatcher);
                client.setListener(connection);
                return client;
            }
        });

        log.info("Listening on {}", port);

        eventService.run();
    }
}
