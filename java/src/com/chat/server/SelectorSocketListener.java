package com.chat.server;

import com.chat.ChatroomRepository;
import com.chat.MessageRepository;
import com.chat.UserRepository;
import com.chat.impl.ByteBufferParserListener;
import com.chat.impl.ByteBufferStream;
import com.chat.msgs.MessageDispatcher;
import com.chat.msgs.V1Dispatcher;
import com.chat.msgs.ValidationError;
import com.chat.select.ClientSocket;
import com.chat.select.EventService;
import com.chat.select.SocketListener;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
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
public class SelectorSocketListener implements SocketListener {
    private final Selector selector;
    private final EventService eventService;

    private final MessageDispatcher[] dispatchers = new MessageDispatcher[10];
    private final Map<ClientSocket, ClientState> socketToStateMap = new HashMap<>(128);
    private final ChatServerImpl server;

    public SelectorSocketListener(Selector selector, EventService eventService, int port, UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) throws IOException {
        this.server = new ChatServerImpl(userRepo, chatroomRepo, messageRepo);
        this.dispatchers[V1Dispatcher.VERSION_ID] = new V1Dispatcher(server, userRepo, chatroomRepo);
        this.selector = selector;
        this.eventService = eventService;

        eventService.createServerSocket(this, port);
        System.out.println("Listening on " + port);

        eventService.run();
    }

    private void processMessages(ClientSocket socket, final ClientState state) {
        try {
            state.stream.read(new ByteBufferParserListener() {
                @Override
                public void onParsed(ByteBuffer buffer) throws InterruptedException, ExecutionException, ValidationError, IOException {
                    if (state.dispatcher == null) {
                        setupDispatcher(state, buffer);
                    }
                    else {
                        state.dispatcher.runOnce(state.stream);
                    }
                }
            });
        } catch (EOFException e) {
            System.out.println("Customer hung up " + socket);
            disconnect(socket);
        } catch (IOException e) {
            System.out.println("Cannot write to stream: " + e.getMessage());
            disconnect(socket);
        } catch (ValidationError e) {
            System.err.println("Validation error:  " + e.getMessage());
            e.printStackTrace();
            disconnect(socket);
        } catch (InterruptedException e) {
            System.err.println("Thread interruption error:  " + e.getMessage());
            e.printStackTrace();
            disconnect(socket);
        } catch (ExecutionException e) {
            System.err.println("Future execution error:  " + e.getMessage());
            e.printStackTrace();
            disconnect(socket);
        }
    }

    private void setupDispatcher(ClientState state, ByteBuffer slice) throws ValidationError, IOException {
        byte type = slice.get();
        if (type != 16)
            throw new ValidationError("Must send Connect first");

        int APIVersion = slice.getInt();
        byte[] bytes = new byte[slice.getShort()];
        slice.get(bytes);
        String UUID = new String(bytes, "UTF-8");

        state.dispatcher = dispatchers[APIVersion];
        state.stream.setAPIVersion(APIVersion);
        state.stream.setUUID(UUID);

        server.addConnection(state.stream);
    }

    private void disconnect(ClientSocket socket) {
        try {
            if (socket != null) {
                System.out.println("Disconnecting client " + socket);

                ClientState state = socketToStateMap.remove(socket);

                if (state != null && state.dispatcher != null) {
                    server.removeConnection(state.stream);
                }

                socket.close();
            }
        } catch (IOException e) {
            // do nothing
        }
    }

    @Override
    public void onConnect(ClientSocket clientSocket) {
        try {
            ByteBufferStream stream = new ByteBufferStream(clientSocket);
            socketToStateMap.put(clientSocket, new ClientState(stream));
        } catch (IOException e) {
            disconnect(clientSocket);
        }
    }

    @Override
    public void onReadAvailable(ClientSocket clientSocket) {
        ClientState state = socketToStateMap.get(clientSocket);
        processMessages(clientSocket, state);
    }

    @Override
    public void onWriteAvailable(ClientSocket clientSocket) {
        ClientState state = socketToStateMap.get(clientSocket);

        try {
            state.stream.writeMessages();
        } catch (IOException e) {
            System.err.println("Error writing to stream " + clientSocket);
            disconnect(clientSocket);
        }
    }

    private static class ClientState {
        final ByteBufferStream stream;
        MessageDispatcher dispatcher;

        ClientState(ByteBufferStream stream) {
            this.stream = stream;
        }
    }
}
