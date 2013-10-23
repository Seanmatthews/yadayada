package com.chat;

import com.chat.server.ChatStreamSocketListener;
import com.chat.impl.*;

import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/21/13
 * Time: 4:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainDB {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, SQLException, ClassNotFoundException {
        int port = Integer.parseInt(args[0]);

        // Setup the connection with the DB
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://userdb01.cehtpxvzecp2.us-west-2.rds.amazonaws.com:3306/userdb",
                "admin",
                "admin123");

        AwsRdsUserRepository userRepo = new AwsRdsUserRepository(connection);

        InMemoryChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        chatroomRepo.createChatroom(userRepo.login("admin", "admin", null).get().getUser(), "Global");

        MessageRepository repo = new InMemoryMessageRepository();

        new ChatStreamSocketListener(port, userRepo, chatroomRepo, repo);
    }
}
