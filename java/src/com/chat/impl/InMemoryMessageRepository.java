package com.chat.impl;

import com.chat.ChatMessage;
import com.chat.Chatroom;
import com.chat.MessageRepository;
import com.chat.User;
import org.apache.logging.log4j.Logger;

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
    private final Logger logger;

    public InMemoryMessageRepository(Logger logger) {
        this.logger = logger;
    }

    @Override
    public ChatMessage create(Chatroom chatroom, User sender, String message) {
        ChatMessage msg = new ChatMessage(nextMessageId.getAndIncrement(), chatroom, sender, message, System.currentTimeMillis());
        // don't store these currently
        return msg;
    }
}
