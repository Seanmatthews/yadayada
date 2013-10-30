package com.chat.server;

import com.chat.*;
import com.chat.impl.NonBlockingByteBufferStream;
import com.chat.msgs.MessageDispatcher;
import com.chat.msgs.MessageDispatcherFactory;
import com.chat.msgs.ValidationError;

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectorSocketListener implements SelectRequester {
    private final MessageDispatcherFactory factory;
    private final Selector selector;
    private final Map<SocketChannel, ClientState> channelToStateMap = new HashMap<>(128);
    private final Queue<SelectChangeRequest> requests = new ConcurrentLinkedQueue<>();

    public SelectorSocketListener(int port, UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) throws IOException {
        factory = new MessageDispatcherFactory(new ChatServerImpl(userRepo, chatroomRepo, messageRepo), userRepo, chatroomRepo);
        selector = Selector.open();

        initServerSocket(port);
        listen();
    }

    private void initServerSocket(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Listening on " + port);
    }

    private void listen() throws IOException {
        while (true) {
            SelectChangeRequest request;
            while((request = requests.poll()) != null) {
                switch (request.type) {
                    case REGISTER:
                        request.socket.register(selector, request.ops);
                        break;
                    case CHANGEOPS:
                        SelectionKey key = request.socket.keyFor(selector);
                        key.interestOps(request.ops);
                        break;
                }
            }

            if (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while(iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (processKey(key)) {

                    }
                    iterator.remove();
                }
            }
        }
    }

    private boolean processKey(SelectionKey key) {
        boolean finished = true;

        try {
            if (key.isValid()) {
                // accepting a client socket
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    acceptClient(channel);
                }

                // reading a client socket
                if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    readMessage(clientChannel);
                }

                // writing to a client socket
                if (key.isWritable()) {
                    System.out.println("Write");
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    finished = writeMessage(clientChannel);
                }
            }
        }
        catch(CancelledKeyException e) {
            // nothing
        }

        return finished;
    }

    private boolean writeMessage(SocketChannel clientChannel) {
        ClientState state = channelToStateMap.get(clientChannel);

        try {
            return state.stream.writeMessages();
        } catch (IOException e) {
            System.err.println("Error writing to stream " + clientChannel);
            disconnect(clientChannel);
        }

        return true;
    }

    private void acceptClient(ServerSocketChannel channel)  {
        try {
            SocketChannel clientChannel = channel.accept();
            clientChannel.configureBlocking(false);

            NonBlockingByteBufferStream stream = new NonBlockingByteBufferStream(clientChannel, this);
            ClientState state = new ClientState(stream);
            channelToStateMap.put(clientChannel, state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage(SocketChannel clientChannel) {
        ClientState state = channelToStateMap.get(clientChannel);
        ByteBuffer inputBuffer = state.input;

        try {
            int read = clientChannel.read(inputBuffer);

            if (read == -1) {
                // end of stream
                disconnect(clientChannel);
            }
        } catch (IOException e) {
            System.err.println("Error reading from the stream " + clientChannel);
            disconnect(clientChannel);
        }

        inputBuffer.flip();
        processMessages(clientChannel, state, inputBuffer);
        inputBuffer.compact();
    }

    private void processMessages(SocketChannel clientChannel, ClientState state, ByteBuffer inputBuffer) {
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
                System.out.println("Customer hung up " + clientChannel);
                disconnect(clientChannel);
                return;
            } catch (IOException e) {
                System.err.println("Cannot write to stream: " + e.getMessage());
                e.printStackTrace();
                disconnect(clientChannel);
                return;
            } catch (ValidationError e) {
                System.err.println("Validation error:  " + e.getMessage());
                e.printStackTrace();
                disconnect(clientChannel);
                return;
            } catch (InterruptedException e) {
                System.err.println("Thread interruption error:  " + e.getMessage());
                e.printStackTrace();
                disconnect(clientChannel);
                return;
            } catch (ExecutionException e) {
                System.err.println("Future execution error:  " + e.getMessage());
                e.printStackTrace();
                disconnect(clientChannel);
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

    private void disconnect(SocketChannel clientChannel) {
        try {
            if (clientChannel != null) {
                System.out.println("Disconnecting client " + clientChannel);

                ClientState state = channelToStateMap.remove(clientChannel);

                if (state != null && state.dispatcher != null) {
                    state.dispatcher.removeConnection();
                }

                clientChannel.close();
            }
        } catch (IOException e) {
            // do nothing
        }
    }

    @Override
    public void addChangeRequest(SelectChangeRequest request) {
        requests.add(request);
        selector.wakeup();
    }

    private static class ClientState {
        MessageDispatcher dispatcher;
        final NonBlockingByteBufferStream stream;
        final ByteBuffer input;

        ClientState(NonBlockingByteBufferStream stream) {
            this.stream = stream;
            this.input = ByteBuffer.allocate(1024);
        }
    }
}
