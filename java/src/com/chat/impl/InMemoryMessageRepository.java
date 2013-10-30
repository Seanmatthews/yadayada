package com.chat.impl;

import com.chat.Chatroom;
import com.chat.Message;
import com.chat.MessageRepository;
import com.chat.User;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class InMemoryMessageRepository implements MessageRepository {
    private final AtomicLong nextMessageId = new AtomicLong(1);

    @Override
    public Message create(Chatroom chatroom, User sender, String message) {
        Message msg = new Message(nextMessageId.getAndIncrement(), chatroom, sender, message, System.currentTimeMillis());
        // don't store these currently
        return msg;
    }
}
