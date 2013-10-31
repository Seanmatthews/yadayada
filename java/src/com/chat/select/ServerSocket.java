package com.chat.select;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 9:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ServerSocket {
    ServerSocket open() throws IOException;
    void close() throws IOException;
    ClientSocket accept() throws IOException;
}
