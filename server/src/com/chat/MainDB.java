package com.chat;

import com.chat.server.impl.*;

import java.io.IOException;
import java.sql.SQLException;
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

        AwsRdsUserRepository userRepo = new AwsRdsUserRepository("jdbc:mysql://userdb01.cehtpxvzecp2.us-west-2.rds.amazonaws.com:3306/userdb", "admin", "admin123");
        InMemoryChatroomRepository chatroomRepo = new InMemoryChatroomRepository();

        MessageRepository repo = new InMemoryMessageRepository();

        new ChatStreamServer(port, userRepo, chatroomRepo, repo);
    }
}
