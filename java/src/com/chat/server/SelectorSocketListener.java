package com.chat.server;

import com.chat.*;
import com.chat.impl.NonBlockingByteBufferStream;
import com.chat.msgs.MessageDispatcher;
import com.chat.msgs.MessageDispatcherFactory;
import com.chat.msgs.ValidationError;
import com.chat.select.ClientSocket;
import com.chat.select.EventService;
import com.chat.select.SocketListener;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
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

    private final MessageDispatcherFactory factory;
    private final Map<ClientSocket, ClientState> socketToStateMap = new HashMap<>(128);

    public SelectorSocketListener(Selector selector, EventService eventService, int port, UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) throws IOException {
        this.factory = new MessageDispatcherFactory(new ChatServerImpl(userRepo, chatroomRepo, messageRepo), userRepo, chatroomRepo);
        this.selector = selector;
        this.eventService = eventService;

        eventService.createServerSocket(this, port);
        System.out.println("Listening on " + port);

        eventService.run();
    }

    private void processMessages(ClientSocket socket, ClientState state, ByteBuffer inputBuffer) {
        while(true) {
            // do we have enough bytes for message size?
            if (inputBuffer.remaining() < 2)
                return;

            // get message size
            short length = inputBuffer.getShort(inputBuffer.position());

            // do we have enough bytes remaining in the message?
            if (inputBuffer.remaining() < 2 + length)
                return;

            // Slice for the message - skip message size
            ByteBuffer slice = inputBuffer.slice();
            slice.position(slice.position() + 2);
            slice.limit(slice.position() + length);

            try {
                if (state.dispatcher == null) {
                    setupDispatcher(state, slice);
                }
                else {
                    state.stream.onReadAvailable(slice);
                    state.dispatcher.runOnce();
                }
            } catch (EOFException e) {
                System.out.println("Customer hung up " + socket);
                disconnect(socket);
                return;
            } catch (IOException e) {
                System.err.println("Cannot write to stream: " + e.getMessage());
                e.printStackTrace();
                disconnect(socket);
                return;
            } catch (ValidationError e) {
                System.err.println("Validation error:  " + e.getMessage());
                e.printStackTrace();
                disconnect(socket);
                return;
            } catch (InterruptedException e) {
                System.err.println("Thread interruption error:  " + e.getMessage());
                e.printStackTrace();
                disconnect(socket);
                return;
            } catch (ExecutionException e) {
                System.err.println("Future execution error:  " + e.getMessage());
                e.printStackTrace();
                disconnect(socket);
                return;
            }

            inputBuffer.position(inputBuffer.position() + length + 2);
        }
    }

    private void setupDispatcher(ClientState state, ByteBuffer slice) throws ValidationError, UnsupportedEncodingException {
        byte type = slice.get();
        if (type != 16)
            throw new ValidationError("Must send Connect first");

        int APIVersion = slice.getInt();
        byte[] bytes = new byte[slice.getShort()];
        slice.get(bytes);
        String UUID = new String(bytes, "UTF-8");
        state.dispatcher = factory.getDispatcher(APIVersion, state.stream, UUID);
    }

    private void disconnect(ClientSocket socket) {
        try {
            if (socket != null) {
                System.out.println("Disconnecting client " + socket);

                ClientState state = socketToStateMap.remove(socket);

                if (state != null && state.dispatcher != null) {
                    state.dispatcher.removeConnection();
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
            NonBlockingByteBufferStream stream = new NonBlockingByteBufferStream(clientSocket);
            socketToStateMap.put(clientSocket, new ClientState(stream));
        } catch (IOException e) {
            disconnect(clientSocket);
        }
    }

    @Override
    public void onReadAvailable(ClientSocket clientSocket) {
        ClientState state = socketToStateMap.get(clientSocket);
        ByteBuffer inputBuffer = state.input;

        try {
            int read = clientSocket.read(inputBuffer);

            if (read == -1) {
                // end of stream
                disconnect(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Error reading from the stream " + clientSocket);
            disconnect(clientSocket);
        }

        inputBuffer.flip();
        processMessages(clientSocket, state, inputBuffer);
        inputBuffer.compact();
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
        MessageDispatcher dispatcher;
        final NonBlockingByteBufferStream stream;
        final ByteBuffer input;

        ClientState(NonBlockingByteBufferStream stream) {
            this.stream = stream;
            this.input = ByteBuffer.allocateDirect(1024);
        }
    }
}
