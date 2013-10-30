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
        CHANGEOPS
    }

    public SocketChannel socket;
    public ChangeRequestType type;
    public int ops;

    public SelectChangeRequest(SocketChannel socket, ChangeRequestType type, int ops) {
        this.socket = socket;
        this.type = type;
        this.ops = ops;
    }
}