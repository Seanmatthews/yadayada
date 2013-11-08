package com.chat.server;

import com.chat.ClientConnection;
import com.chat.client.ChatClientDispatcher;
import com.chat.msgs.Message;
import com.chat.msgs.MessageDispatcher;
import com.chat.msgs.ValidationError;
import com.chat.select.ClientSocket;
import com.chat.select.EventService;
import com.chat.util.TwoByteLengthMessageCracker;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;
import com.chat.util.tcp.TCPCrackerClient;
import com.chat.util.tcp.TCPCrackerClientListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerClientConnection implements ClientConnection {
    private final Logger log = LogManager.getLogger();

    private final Queue<Message> queue = new LinkedList<>();

    private final MessageDispatcher dispatcher;
    private final TCPCrackerClient socket;

    public ServerClientConnection(TCPCrackerClient socket, MessageDispatcher dispatcher) throws IOException {
        this.socket = socket;
        this.dispatcher = dispatcher;
    }

    public void onCracked(ReadBuffer slice) {
        // skip size
        slice.advance(2);

        try {
            dispatcher.onMessage(this, slice);
        } catch (EOFException e) {
            log.debug("Customer hung up");
            socket.disconnect();
        } catch (IOException e) {
            log.error("Cannot write to stream " + socket, e);
            socket.disconnect();
        } catch (ValidationError e) {
            log.info("Validation error " + socket, e);
            socket.disconnect();
        } catch (InterruptedException e) {
            log.debug("Thread interruption");
            socket.disconnect();
        } catch (ExecutionException e) {
            log.error("Future execution exception", e);
            socket.disconnect();
        } catch (RuntimeException e) {
            log.error("Unknown exception", e);
            socket.disconnect();
        }
    }

    @Override
    public void close() {
        socket.disconnect();
    }

    @Override
    public void sendMessage(Message message, boolean immediate) throws IOException {
        if (immediate) {
            ReadWriteBuffer output = socket.getWriteBuffer();

            // write to buffer
            message.write(output);
            socket.write();

            // Message got queued up
            if (output.position() != 0) {
                throw new IOException("Too many messages in the queue to send. Terminating.");
            }
        }
        else {
            if (queue.offer(message)) {
                socket.enableWrite(true);
            }
            else {
                throw new IOException("Too many messages in the queue to send. Terminating.");
            }
        }
    }

    public void onWriteAvailable(ReadWriteBuffer output) {
        Message msg;

        while ((msg = queue.poll()) != null)  {
            msg.write(output);
        }
    }
}
