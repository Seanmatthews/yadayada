package com.chat.select;

import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 9:59 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ClientSocket {
    void setListener(ClientSocketListener listener);

    void enableConnect(boolean val);
    void enableRead(boolean val);
    void enableWrite(boolean val);

    int read(ReadBuffer buffer);
    void write(ReadWriteBuffer output);

    void connect(String host, int port) throws IOException;
    void close();
}
