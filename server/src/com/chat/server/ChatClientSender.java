package com.chat.server;

import com.chat.Chatroom;
import com.chat.Message;
import com.chat.User;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/20/13
 * Time: 9:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatClientSender {
    void sendRegisterAccept(User user) throws IOException;
    void sendRegisterReject(User user, String reason) throws IOException;
    void sendLoginAccept(User user) throws IOException;
    void sendLoginReject(String reason) throws IOException;
    void sendMessage(Message message) throws IOException;
    void sendChatroom(Chatroom chatroom) throws IOException;
    void sendJoinChatroom(Chatroom chatroom, User user) throws IOException;
    void close();
}
