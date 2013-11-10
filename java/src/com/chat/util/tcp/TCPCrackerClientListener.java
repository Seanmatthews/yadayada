package com.chat.util.tcp;

import com.chat.util.buffer.ReadBuffer;
import com.chat.util.buffer.ReadWriteBuffer;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/7/13
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TCPCrackerClientListener {
    void onConnect(TCPCrackerClient client);
    void onDisconnect(TCPCrackerClient client);
    void onCracked(TCPCrackerClient client, ReadBuffer slice);
}
