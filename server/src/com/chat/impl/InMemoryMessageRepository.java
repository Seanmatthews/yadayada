package com.chat.impl;

import com.chat.Chatroom;
import com.chat.Message;
import com.chat.MessageRepository;
import com.chat.User;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class InMemoryMessageRepository implements MessageRepository {
    private long nextMessageId = 1;

    @Override
    public Message create(Chatroom chatroom, User sender, String message) {
        Message msg = new Message();
        msg.id = nextMessageId++;
        msg.chatroom = chatroom;
        msg.sender = sender;
        msg.message = message;
        return msg;
    }
}
