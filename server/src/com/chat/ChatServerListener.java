package com.chat;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatServerListener implements Runnable {
    private final ChatServer server;
    private final Socket socket;
    private final UserRepository userRepo;
    private final ChatroomRepository chatroomRepo;

    public ChatServerListener(ChatServer server, Socket socket, UserRepository userRepo, ChatroomRepository chatroomRepo) {
        this.server = server;
        this.socket = socket;
        this.userRepo = userRepo;
        this.chatroomRepo = chatroomRepo;
    }

    @Override
    public void run() {
        try {
            DataInputStream din = new DataInputStream( socket.getInputStream() );

            while(true) {
                short length = din.readShort();
                byte msgType = din.readByte();
                MessageTypes type = MessageTypes.lookup(msgType);

                if (type == null) {
                    byte[] msg = new byte[length];
                    din.read(msg);
                    System.out.println("Received unknown msg: " + new String(msg));
                    continue;
                }

                switch (type) {
                    case SEARCH_CHATROOMS:
                        server.searchChatrooms(socket);
                        break;

                    case CREATE_CHATROOM:
                        User ccUser = getAndValidateUser(din.readLong());
                        String ccChatroomName = din.readUTF();
                        server.createChatroom(socket, ccUser, ccChatroomName);
                        break;

                    case JOIN_CHATROOM:
                        User jcUser = getAndValidateUser(din.readLong());
                        Chatroom jcChatroom = getAndValidateChatroom(din.readLong());
                        server.joinChatroom(socket, jcUser, jcChatroom);
                        break;

                    case LEAVE_CHATROOM:
                        User lcUser = getAndValidateUser(din.readLong());
                        Chatroom lcChatroom = getAndValidateChatroom(din.readLong());
                        server.leaveChatroom(socket, lcUser, lcChatroom);
                        break;

                    case REGISTER:
                        System.out.println("about to register...");
                        String regLogin = din.readUTF();
                        String regPassword = din.readUTF();
                        server.registerUser(socket, regLogin, regPassword);
                        System.out.println("registered a fuckin user");
                        break;

                    case LOGIN:
                        String logLogin = din.readUTF();
                        String logPassword = din.readUTF();
                        server.login(socket, logLogin, logPassword);
                        break;

                    case SUBMIT_MESSAGE:
                        User user = getAndValidateUser(din.readLong());
                        Chatroom chatroom = getAndValidateChatroom(din.readLong());
                        String message = din.readUTF();
                        System.out.println("Sending " + message);
                        server.newMessage(socket, user, chatroom, message);
                        break;
                }
            }
        } catch (EOFException e) {
            // nothing
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ValidationError e) {
            System.err.println("Validation error. " + e.getMessage());
            e.printStackTrace();
        } finally {
            server.removeConnection(socket);
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
