package com.chat.server;

import com.chat.*;
import com.chat.server.impl.ChatServerSenderImpl;

import java.io.EOFException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatServerListener implements Runnable {
    private final ChatServer server;
    private final Connection connection;
    private final UserRepository userRepo;
    private final ChatroomRepository chatroomRepo;
    private final ChatClientSender sender;

    public ChatServerListener(ChatServer server, Connection connection, UserRepository userRepo, ChatroomRepository chatroomRepo) {
        this.server = server;
        this.connection = connection;
        this.userRepo = userRepo;
        this.chatroomRepo = chatroomRepo;
        this.sender = new ChatServerSenderImpl(connection);
    }

    @Override
    public void run() {
        try {
            while(true) {
                short length = connection.readShort();
                byte msgType = connection.readByte();
                MessageTypes type = MessageTypes.lookup(msgType);

                if (type == null) {
                    byte[] msg = connection.read(length);
                    System.out.println("Received unknown msg: " + new String(msg));
                    continue;
                }

                switch (type) {
                    case SEARCH_CHATROOMS:
                        server.searchChatrooms(sender);
                        break;

                    case CREATE_CHATROOM:
                        User ccUser = getAndValidateUser(connection.readLong());
                        String ccChatroomName = connection.readString();
                        server.createChatroom(sender, ccUser, ccChatroomName);
                        break;

                    case JOIN_CHATROOM:
                        User jcUser = getAndValidateUser(connection.readLong());
                        Chatroom jcChatroom = getAndValidateChatroom(connection.readLong());
                        server.joinChatroom(sender, jcUser, jcChatroom);
                        break;

                    case LEAVE_CHATROOM:
                        User lcUser = getAndValidateUser(connection.readLong());
                        Chatroom lcChatroom = getAndValidateChatroom(connection.readLong());
                        server.leaveChatroom(sender, lcUser, lcChatroom);
                        break;

                    case REGISTER:
                        String regLogin = connection.readString();
                        String regPassword = connection.readString();
                        String handle = connection.readString();
                        server.registerUser(sender, regLogin, regPassword, handle);
                        System.out.println("registered a fuckin user");
                        break;

                    case QUICK_REGISTER:
                        String qrHandle = connection.readString();
                        server.quickRegisterUser(sender, qrHandle);
                        System.out.println("registered a fuckin user");
                        break;

                    case LOGIN:
                        String logLogin = connection.readString();
                        String logPassword = connection.readString();
                        server.login(sender, logLogin, logPassword);
                        break;

                    case SUBMIT_MESSAGE:
                        User user = getAndValidateUser(connection.readLong());
                        Chatroom chatroom = getAndValidateChatroom(connection.readLong());
                        String msg = connection.readString();
                        System.out.println("Sending " + msg);
                        server.newMessage(sender, user, chatroom, msg);
                        break;
                }
            }
        } catch (EOFException e) {
            System.out.println("Customer hang up");
        } catch (IOException e) {
            System.out.println("Cannot write to connection");
            e.printStackTrace();
        } catch (ValidationError e) {
            System.err.println("Validation error " + e.getMessage());
            e.printStackTrace();
        } finally {
            server.removeConnection(sender);
        }
    }

    private User getAndValidateUser(long userId) throws ValidationError {
        User user = userRepo.get(userId);

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
