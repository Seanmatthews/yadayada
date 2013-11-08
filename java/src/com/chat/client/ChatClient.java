package com.chat.client;

import com.chat.ChatMessage;
import com.chat.Chatroom;
import com.chat.User;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ChatClient {
    void onChatroom(Chatroom chatroom) throws IOException;
    void onMessage(ChatMessage message);
    void onJoinedChatroom(Chatroom chat, User user);
    void onLeftChatroom(Chatroom chatroom, User user);
    void onJoinedChatroomReject(String reason);
    void onLoginAccept(long userId);
}
