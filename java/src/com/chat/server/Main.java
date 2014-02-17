package com.chat.server;

import com.chat.ChatroomRepository;
import com.chat.MessageRepository;
import com.chat.User;
import com.chat.UserRepository;
import com.chat.impl.AwsRdsUserRepository;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;
import com.chat.impl.STMessageRepository;
import com.chat.select.impl.EventServiceImpl;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/20/13
 * Time: 10:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, SQLException, ClassNotFoundException {
        CommandLineParser parser = new BasicParser();
        Options myOptions = new Options();
        myOptions.addOption("userrepository", true, "memory or database");
        myOptions.addOption("port", true, "TCP port");
        myOptions.addOption("sqldriver", true, "Driver for SQL Connection");
        myOptions.addOption("sqlurl", true, "URL of SQL Server");
        myOptions.addOption("sqluser", true, "SQL Username");
        myOptions.addOption("sqlpassword", true, "SQL Username");
        CommandLine options;

        Logger logger = LogManager.getLogger();

        try {
            options = parser.parse(myOptions, args);
        } catch (ParseException e) {
            logger.error("Error parsing options", e);
            return;
        }

        int port = Integer.parseInt(options.getOptionValue("port", "5000"));

        UserRepository userRepo;
        User admin;

        String repo = options.getOptionValue("userrepository", "memory");
        if (repo.equals("database")) {
            String driver = options.getOptionValue("sqldriver");
            String connectionString = options.getOptionValue("sqlurl");
            String username = options.getOptionValue("sqluser");
            String password = options.getOptionValue("sqlpassword");

            // Setup the connection with the DB
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(connectionString, username, password);
            userRepo = new AwsRdsUserRepository(connection);
            admin = userRepo.login("admin", "admin", null).get().getUser();

            logger.info("Loaded database user repository {} {} Username={} Password={}", driver, connectionString, username, password);
        }
        else {
            userRepo = new InMemoryUserRepository();
            admin = userRepo.registerUser("admin", "admin", "admin", "ADMIN_UUID", 12155551212L, null).get().getUser();

            logger.info("Loaded in-memory user repository");
        }

        MessageRepository messageRepo = new STMessageRepository();
        //ChatroomRepository chatroomRepo = new STChatroomRepository();
        ChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        chatroomRepo.createChatroom(admin, "Global", 0, 0, 0);

        new ServerListener(new EventServiceImpl(), port, userRepo, chatroomRepo, messageRepo);
    }
}
