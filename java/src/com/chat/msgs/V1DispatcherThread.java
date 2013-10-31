package com.chat.msgs;

import com.chat.ChatroomRepository;
import com.chat.UserRepository;
import com.chat.msgs.v1.ClientConnection;
import com.chat.server.ChatServer;

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
    private final ClientConnection connection;

    public V1DispatcherThread(ChatServer server, UserRepository userRepo, ChatroomRepository chatroomRepo, ClientConnection connection) {
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
            System.out.println("Customer hung up");
        } catch (IOException e) {
            System.err.println("Cannot write to stream: " + e.getMessage());
            e.printStackTrace();
        } catch (ValidationError e) {
            System.err.println("Validation error:  " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread interruption error:  " + e.getMessage());
            e.printStackTrace();
        } catch (ExecutionException e) {
            System.err.println("Future execution error:  " + e.getMessage());
            e.printStackTrace();
        } finally {
            server.removeConnection(connection);
        }
    }
}
