package com.chat.server;

import com.chat.*;
import com.chat.server.impl.ClientConnectionImpl;
import com.chat.msgs.v1.*;

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
public class ChatServerDispatcher implements Runnable {
    private final ChatServer server;
    private final UserRepository userRepo;
    private final ChatroomRepository chatroomRepo;
    private final ClientConnection connection;

    public ChatServerDispatcher(ChatServer server, BinaryStream stream, UserRepository userRepo, ChatroomRepository chatroomRepo) {
        this.server = server;
        this.userRepo = userRepo;
        this.chatroomRepo = chatroomRepo;
        this.connection = new ClientConnectionImpl(stream);
    }

    @Override
    public void run() {
        try {
            while(true) {
                MessageTypes type = connection.recvMsgType();

                if (type == MessageTypes.CONNECT) {
                    ConnectMessage cMsg = connection.recvConnect();
                    server.connect(connection, cMsg.getAPIVersion(), cMsg.getUUID());
                    continue;
                }
                else {
                    if (connection.getUUID() == null) {
                        System.err.println("Customer sent us " + type + " instead of connect");
                        // we haven't called connect before
                        server.removeConnection(connection);
                        return;
                    }
                }

                switch (type) {
                    case SEARCH_CHATROOMS:
                        SearchChatroomsMessage scMsg = connection.recvSearchChatrooms();
                        server.searchChatrooms(connection);
                        break;

                    case CREATE_CHATROOM:
                        CreateChatroomMessage ccMsg = connection.recvCreateChatroom();
                        User ccUser = getAndValidateUser(ccMsg.getOwnerId());
                        server.createChatroom(connection, ccUser, ccMsg.getChatroomName());
                        break;

                    case JOIN_CHATROOM:
                        JoinChatroomMessage jcMsg = connection.recvJoinChatroom();
                        User jcUser = getAndValidateUser(jcMsg.getUserId());
                        Chatroom jcChatroom = getAndValidateChatroom(jcMsg.getChatroomId());
                        server.joinChatroom(connection, jcUser, jcChatroom);
                        break;

                    case LEAVE_CHATROOM:
                        LeaveChatroomMessage lcMsg = connection.recvLeaveChatroom();
                        User lcUser = getAndValidateUser(lcMsg.getUserId());
                        Chatroom lcChatroom = getAndValidateChatroom(lcMsg.getChatroomId());
                        server.leaveChatroom(connection, lcUser, lcChatroom);
                        break;

                    case REGISTER:
                        RegisterMessage rMsg = connection.recvRegister();
                        server.registerUser(connection, rMsg.getUserName(), rMsg.getPassword(), rMsg.getHandle());
                        System.out.println("registered a fuckin user");
                        break;

                    case LOGIN:
                        LoginMessage lMsg = connection.recvLogin();
                        server.login(connection, lMsg.getUserName(), lMsg.getPassword());
                        break;

                    case SUBMIT_MESSAGE:
                        SubmitMessageMessage smMsg = connection.recvSubmitMessage();
                        User user = getAndValidateUser(smMsg.getUserId());
                        Chatroom chatroom = getAndValidateChatroom(smMsg.getChatroomId());
                        System.out.println("Sending " + smMsg.getMessage());
                        server.newMessage(connection, user, chatroom, smMsg.getMessage());
                        break;

                    default:
                        System.err.println("Unhandled message: " + type);
                        connection.recvUnknown();
                }
            }
        } catch (EOFException e) {
            System.out.println("Customer hang up");
        } catch (IOException e) {
            System.out.println("Cannot write to stream");
            e.printStackTrace();
        } catch (ValidationError e) {
            System.err.println("Validation error " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            server.removeConnection(connection);
        }
    }

    private User getAndValidateUser(long userId) throws ValidationError, ExecutionException, InterruptedException {
        User user = userRepo.get(userId, null).get().getUser();

        if (user == null) {
            throw new ValidationError("Unknown user: " + userId);
        }

        server.mapClientConnectionToUser(connection, user);
        return user;
    }

    private Chatroom getAndValidateChatroom(long chatroomId) throws ValidationError {
        Chatroom chatroom = chatroomRepo.get(chatroomId);

        if (chatroom == null) {
            throw new ValidationError("Unknown chatroom: " + chatroomId);
        }

        return chatroom;
    }

    private static class ValidationError extends Exception {
        private final String message;

        public ValidationError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
