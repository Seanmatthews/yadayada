package com.chat.util.tcp;

import com.chat.select.ClientSocket;
import com.chat.util.ByteCracker;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 11/10/13
 * Time: 7:55 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TCPCrackerClientFactory {
    TCPCrackerClient createClient(ByteCracker cracker, ClientSocket socket);
}
