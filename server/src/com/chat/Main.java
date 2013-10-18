package com.chat;

import com.chat.ChatServer;
import com.chat.impl.ChatroomRepositoryImpl;
import com.chat.impl.UserRepositoryImpl;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);

        UserRepositoryImpl userRepo = new UserRepositoryImpl();
        User admin = userRepo.registerUser("admin", "admin");
        ChatroomRepositoryImpl chatroomRepo = new ChatroomRepositoryImpl();
        chatroomRepo.createChatroom(admin, "Global");

        new ChatServer(port, userRepo, chatroomRepo);
    }
}
