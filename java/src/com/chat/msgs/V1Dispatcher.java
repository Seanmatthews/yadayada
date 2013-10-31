package com.chat.msgs;

import com.chat.*;
import com.chat.msgs.v1.*;
import com.chat.msgs.v1.ClientConnection;
import com.chat.server.ChatServer;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class V1Dispatcher implements MessageDispatcher, Runnable {
    public static final int VERSION_ID = 1;

    private final ChatServer server;
    private final UserRepository userRepo;
    private final ChatroomRepository chatroomRepo;
    private final ClientConnection connection;

    public V1Dispatcher(ChatServer server, ClientConnection connection, UserRepository userRepo, ChatroomRepository chatroomRepo) {
        this.server = server;
        this.userRepo = userRepo;
        this.chatroomRepo = chatroomRepo;
        this.connection = connection;

        server.addConnection(connection);
    }

    @Override
    public void run() {
        try {
            while(true) {
                runOnce();
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
            removeConnection();
        }
    }

    @Override
    public void removeConnection() {
        server.removeConnection(connection);
    }

    @Override
    public void runOnce() throws IOException, ValidationError, ExecutionException, InterruptedException {
        MessageTypes type = connection.recvMsgType();

        switch (type) {
            case SearchChatrooms:
                SearchChatroomsMessage scMsg = connection.recvSearchChatrooms();
                server.searchChatrooms(connection);
                break;

            case CreateChatroom:
                CreateChatroomMessage ccMsg = connection.recvCreateChatroom();
                User ccUser = getAndValidateUser(ccMsg.getOwnerId());
                server.createChatroom(connection, ccUser, ccMsg.getChatroomName());
                break;

            case JoinChatroom:
                JoinChatroomMessage jcMsg = connection.recvJoinChatroom();
                User jcUser = getAndValidateUser(jcMsg.getUserId());
                Chatroom jcChatroom = getAndValidateChatroom(jcMsg.getChatroomId());
                server.joinChatroom(connection, jcUser, jcChatroom);
                break;

            case LeaveChatroom:
                LeaveChatroomMessage lcMsg = connection.recvLeaveChatroom();
                User lcUser = getAndValidateUser(lcMsg.getUserId());
                Chatroom lcChatroom = getAndValidateChatroom(lcMsg.getChatroomId());
                server.leaveChatroom(connection, lcUser, lcChatroom);
                break;

            case Register:
                RegisterMessage rMsg = connection.recvRegister();
                server.registerUser(connection, rMsg.getUserName(), rMsg.getPassword(), rMsg.getHandle());
                break;

            case Login:
                LoginMessage lMsg = connection.recvLogin();
                server.login(connection, lMsg.getUserName(), lMsg.getPassword());
                break;

            case SubmitMessage:
                SubmitMessageMessage smMsg = connection.recvSubmitMessage();
                User user = getAndValidateUser(smMsg.getUserId());
                Chatroom chatroom = getAndValidateChatroom(smMsg.getChatroomId());
                System.out.println("Sending " + smMsg.getMessage());
                server.newMessage(connection, user, chatroom, smMsg.getMessage());
                break;

            default:
                throw new ValidationError("Unhandled message: " + type);
        }
    }

    private User getAndValidateUser(long userId) throws ValidationError, ExecutionException, InterruptedException {
        User user = userRepo.get(userId, null).get().getUser();

        if (user == null) {
            throw new ValidationError("Unknown user: " + userId);
        }

        return user;
    }

    private Chatroom getAndValidateChatroom(long chatroomId) throws ValidationError {
        Chatroom chatroom = chatroomRepo.get(chatroomId);

        if (chatroom == null) {
            throw new ValidationError("Unknown chatroom: " + chatroomId);
        }

        return chatroom;
    }
}
