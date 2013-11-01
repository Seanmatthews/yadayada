package com.chat.server;

import com.chat.MessageRepository;
import com.chat.User;
import com.chat.UserRepository;
import com.chat.impl.*;
import com.chat.select.impl.EventServiceImpl;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.channels.Selector;
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
        myOptions.addOption("io", true, "blocking or nonblocking");
        CommandLine options;

        try {
            options = parser.parse(myOptions, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
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

            System.out.println("Loaded database user repository");
        }
        else {
            userRepo = new InMemoryUserRepository();
            admin = userRepo.registerUser("admin", "admin", "admin", "ADMIN_UUID", null).get().getUser();

            System.out.println("Loaded in-memory user repository");
        }

        MessageRepository messageRepo = new InMemoryMessageRepository();

        String io = options.getOptionValue("io", "nonblocking");
        if (io.equals("blocking")) {
            InMemoryChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
            chatroomRepo.createChatroom(admin, "Global");
            new StreamSocketListener(port, userRepo, chatroomRepo, messageRepo);
        }
        else {
            SingleThreadChatroomRepository chatroomRepo = new SingleThreadChatroomRepository();
            chatroomRepo.createChatroom(admin, "Global");

            Selector selector = Selector.open();
            new SelectorSocketListener(selector, new EventServiceImpl(selector), port, userRepo, chatroomRepo, messageRepo);
        }

    }
}
