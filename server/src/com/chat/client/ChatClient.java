package com.chat.client;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatClient {
    void onChatroom(long chatroomId, String chatroomName, long ownerUserId, String ownerName) throws IOException;
    void onMessage(String userName, String message);
    void sendMessage(String msg) throws IOException;
}
