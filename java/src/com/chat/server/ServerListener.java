package com.chat.server;

import com.chat.ChatroomRepository;
import com.chat.MessageRepository;
import com.chat.UserRepository;
import com.chat.msgs.MessageDispatcher;
import com.chat.msgs.V1Dispatcher;
import com.chat.msgs.ValidationError;
import com.chat.select.EventService;
import com.chat.util.TwoByteLengthMessageCracker;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.tcp.TCPCrackerClient;
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
public class ServerListener implements TCPCrackerClientListener {
    private final Logger log = LogManager.getLogger();
    private final EventService eventService;
    private final MessageDispatcher dispatcher;

    private final Map<TCPCrackerClient, ServerClientConnection> tcpConnectionMap = new HashMap<>();
    private final ChatServerImpl server;

    public ServerListener(EventService eventService, int port, UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) throws IOException {
        this.eventService = eventService;

        server = new ChatServerImpl(eventService, userRepo, chatroomRepo, messageRepo);
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
    public void onDisconnect(TCPCrackerClient client) {
        ServerClientConnection connection = tcpConnectionMap.remove(client);

        if (connection != null)
            server.removeConnection(connection);
    }

    @Override
    public void onCracked(TCPCrackerClient client, ReadBuffer slice) {
        ServerClientConnection connection = tcpConnectionMap.get(client);

        if (connection != null) {
            try {
                connection.onCracked(slice);
            } catch (EOFException e) {
                log.debug("Customer hung up");
                connection.close();
            } catch (IOException e) {
                log.error("Cannot write to stream " + connection, e);
                connection.close();
            } catch (ValidationError e) {
                log.info("Validation error " + connection, e);
                connection.close();
            } catch (InterruptedException e) {
                log.debug("Thread interruption");
                connection.close();
            } catch (ExecutionException e) {
                log.error("Future execution exception", e);
                connection.close();
            } catch (RuntimeException e) {
                log.error("Unknown exception", e);
                connection.close();
            }
        }
    }
}
