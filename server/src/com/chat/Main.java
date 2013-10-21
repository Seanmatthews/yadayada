package com.chat;

import com.chat.server.impl.ChatStreamServer;
import com.chat.server.impl.InMemoryChatroomRepository;
import com.chat.server.impl.InMemoryMessageRepository;
import com.chat.server.impl.InMemoryUserRepository;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/20/13
 * Time: 10:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);

        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        User admin = userRepo.registerUser("admin", "admin");

        InMemoryChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        chatroomRepo.createChatroom(admin, "Global");

        MessageRepository repo = new InMemoryMessageRepository();

        new ChatStreamServer(port, userRepo, chatroomRepo, repo);
    }
}