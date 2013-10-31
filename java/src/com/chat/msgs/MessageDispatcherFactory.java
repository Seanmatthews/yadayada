package com.chat.msgs;

import com.chat.BinaryStream;
import com.chat.ChatroomRepository;
import com.chat.UserRepository;
import com.chat.server.ChatServer;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/29/13
 * Time: 9:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessageDispatcherFactory {
    private final ChatServer server;
    private final UserRepository userRepo;
    private final ChatroomRepository chatroomRepo;

    public MessageDispatcherFactory(ChatServer server, UserRepository userRepo, ChatroomRepository chatroomRepo) {
        this.server = server;
        this.userRepo = userRepo;
        this.chatroomRepo = chatroomRepo;
    }

    public MessageDispatcher getDispatcher(int apiVersion, BinaryStream stream, String UUID) throws ValidationError {
        if (apiVersion == V1Dispatcher.VERSION_ID) {
            return new V1DispatcherThread(server, userRepo, chatroomRepo, stream);
        }

        throw new ValidationError("Invalid API Version: " + apiVersion);
    }
}
