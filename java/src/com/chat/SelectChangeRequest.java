package com.chat;

import java.nio.channels.SocketChannel;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/30/13
 * Time: 7:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelectChangeRequest {
    public static enum ChangeRequestType {
        REGISTER,
        ENABLE_WRITE,
        DISABLE_WRITE
    }

    public SocketChannel socket;
    public ChangeRequestType type;

    public SelectChangeRequest(SocketChannel socket, ChangeRequestType type) {
        this.socket = socket;
        this.type = type;
    }
}