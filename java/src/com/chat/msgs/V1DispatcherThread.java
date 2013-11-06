package com.chat.msgs;

import com.chat.BinaryStream;
import com.chat.ChatroomRepository;
import com.chat.UserRepository;
import com.chat.server.ChatServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/31/13
 * Time: 12:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class V1DispatcherThread extends V1Dispatcher implements Runnable {
    private final Logger log = LogManager.getLogger();

    private final BinaryStream connection;

    public V1DispatcherThread(ChatServer server, UserRepository userRepo, ChatroomRepository chatroomRepo, BinaryStream connection) {
        super(server, userRepo, chatroomRepo) ;

        this.connection = connection;

        server.addConnection(connection);
    }

    @Override
    public void run() {
        try {
            while(true) {
                runOnce(connection);
            }
        } catch (EOFException e) {
            log.debug("Customer hung up");
        } catch (IOException e) {
            log.error("Cannot write to stream " + connection, e);
        } catch (ValidationError e) {
            log.info("Validation error " + connection, e);
        } catch (InterruptedException e) {
            log.debug("Thread interruption");
        } catch (ExecutionException e) {
            log.error("Future execution exception", e);
        } finally {
            server.removeConnection(connection);
        }
    }
}
