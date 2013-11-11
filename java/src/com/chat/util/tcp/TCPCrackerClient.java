package com.chat.util.tcp;

import com.chat.select.ClientSocket;
import com.chat.select.ClientSocketListener;
import com.chat.util.ByteCracker;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;
import com.chat.util.buffer.impl.WrappedReadBuffer;
import com.chat.util.buffer.impl.WrappedReadWriteByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class TCPCrackerClient implements ClientSocketListener {
    private final Logger log = LogManager.getLogger();

    private final ClientSocket socket;

    private final ByteCracker cracker;
    private TCPCrackerClientListener listener;

    private final ReadBuffer readBuffer;
    private final ReadWriteBuffer writeBuffer;

    public TCPCrackerClient(ByteCracker cracker, ClientSocket socket) {
        this(cracker, socket, null);
    }

    public TCPCrackerClient(ByteCracker cracker, ClientSocket socket, TCPCrackerClientListener listener) {
        this.cracker = cracker;
        this.listener = listener;
        this.readBuffer = new WrappedReadBuffer(ByteBuffer.allocateDirect(1024));
        this.writeBuffer = new WrappedReadWriteByteBuffer(ByteBuffer.allocateDirect(1024));
        this.socket = socket;
    }

    public void setListener(TCPCrackerClientListener listener) {
        this.listener = listener;
    }

    public void connect(String host, int port) throws IOException {
        this.socket.connect(host, port);
    }

    public void close() {
        socket.close();
    }

    public void write() {
        if (writeBuffer.position() > 0) {
            writeBuffer.flip();
            socket.write(writeBuffer);

            if (writeBuffer.hasRemaining()) {
                writeBuffer.compact();
            }
            else {
                writeBuffer.clear();
            }
        }
    }


    public ReadWriteBuffer getWriteBuffer() {
        return writeBuffer;
    }


    @Override
    public void onWriteAvailable(ClientSocket clientSocket) {
        log.debug("onWriteAvailable() {}", clientSocket);

        write();
    }

    @Override
    public void onReadAvailable(ClientSocket clientSocket) {
        log.debug("onReadAvailable() {}", clientSocket);

        int read = socket.read(readBuffer);

        if (read > 0) {
            readBuffer.flip();

            ReadBuffer slice;
            while((slice = cracker.crack(readBuffer)) != null) {
                int newPosition = readBuffer.position() + slice.remaining();
                listener.onCracked(this, slice);
                readBuffer.position(newPosition);
            }

            if (readBuffer.hasRemaining()) {
                readBuffer.compact();
            }
            else {
                readBuffer.clear();
            }
        }
        else {
            readBuffer.clear();
        }
    }

    @Override
    public void onConnect(ClientSocket clientSocket) {
        log.info("Connecting socket {}", clientSocket);
        listener.onConnect(this);
    }

    @Override
    public void onDisconnect(ClientSocket clientSocket) {
        log.info("Disconnecting socket {}", clientSocket);
        listener.onDisconnect(this);
    }

    @Override
    public void onWriteUnavailable(ClientSocket clientSocket) {
        log.error("Could not write everything to socket. Disconnecting");
        close();
    }
}
