package com.chat;

import com.chat.msgs.Message;
import com.chat.util.buffer.ReadWriteBuffer;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ClientConnection {
    void close();
    void sendMessage(Message message) throws IOException;
    void setUser(User user);
    User getUser();
}
