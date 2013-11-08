package com.chat.util.tcp;

import com.chat.select.ClientSocket;
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
public class TCPCrackerClient {
    private final Logger log;

    private ClientSocket socket;

    private final ByteCracker cracker;
    private final TCPCrackerClientListener listener;

    private final ReadBuffer readBuffer;
    private final ReadWriteBuffer writeBuffer;

    public TCPCrackerClient(String name, ByteCracker cracker, TCPCrackerClientListener listener, ClientSocket socket) {
        this.cracker = cracker;
        this.listener = listener;
        this.readBuffer = new WrappedReadBuffer(ByteBuffer.allocateDirect(1024));
        this.writeBuffer = new WrappedReadWriteByteBuffer(ByteBuffer.allocateDirect(1024));
        this.log = LogManager.getLogger(TCPCrackerClient.class + "[" + name + "]");
        this.socket = socket;
    }

    public void enableRead(boolean read) {
        socket.enableRead(read);
    }

    public void enableWrite(boolean write) {
        socket.enableWrite(write);
    }

    public void connect(String host, int port) throws IOException {
        this.socket.connect(host, port);
    }

    public void disconnect() {
        log.info("  Disconnecting Socket");
        this.socket.close();
    }

    public void write() {
        if (writeBuffer.position() > 0) {
            writeBuffer.flip();

            try {
                socket.write(writeBuffer);
            } catch (IOException e) {
                log.error("  Error writing", e);
                disconnect();
                return;
            }

            if (writeBuffer.hasRemaining()) {
                writeBuffer.compact();
                socket.enableWrite(true);
            }
            else {
                writeBuffer.clear();
            }
        }
    }

    public void onWriteAvailable() {
        log.debug("onWriteAvailable()");

        listener.onWriteAvailable(this, writeBuffer);

        write();
    }

    public ReadWriteBuffer getWriteBuffer() {
        return writeBuffer;
    }

    public void onConnect() {
        log.info("Connecting Socket");
        listener.onConnect(this);
    }

    public void onReadAvailable() {
        log.debug("onReadAvailable()");

        int read;

        try {
            read = socket.read(readBuffer);
        } catch (IOException e) {
            log.error("  Error reading", e);
            disconnect();
            return;
        }

        if (read == -1) {
            log.info("  End of File. Disconnecting.");

            // EOF
            disconnect();
            return;
        }

        if (read != 0) {
            readBuffer.flip();

            ReadBuffer slice = null;
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
}
