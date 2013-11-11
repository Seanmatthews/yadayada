package com.chat.select;

import java.nio.channels.SocketChannel;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/10/13
 * Time: 7:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ClientSocketFactory {
    ClientSocket createClickSocket(EventService eventService, SocketChannel channel);
}
