package com.chat;

import com.chat.impl.ChatStreamServer;
import com.chat.impl.ChatroomRepositoryImpl;
import com.chat.impl.InMemoryMessageRepository;
import com.chat.impl.InMemoryUserRepository;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);

        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        User admin = userRepo.registerUser("admin", "admin");

        ChatroomRepositoryImpl chatroomRepo = new ChatroomRepositoryImpl();
        chatroomRepo.createChatroom(admin, "Global");

        MessageRepository repo = new InMemoryMessageRepository();

        new ChatStreamServer(port, userRepo, chatroomRepo, repo);
    }
}
