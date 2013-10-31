package com.chat.select;

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
    void enableRead(boolean val);
    void enableWrite(boolean val);
    void onWriteAvailable();
    void onReadAvailable();
    int read(ByteBuffer buffer) throws IOException;
    void write(ByteBuffer output) throws IOException;
    void close() throws IOException;
}
