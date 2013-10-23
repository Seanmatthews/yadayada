package com.chat.client;

import com.chat.*;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 10:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChatClientDispatcher implements Runnable {
    private final ChatClient client;
    private final BinaryStream connection;
    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;

    public ChatClientDispatcher(ChatClient client, BinaryStream stream, ChatroomRepository chatroomRepo, UserRepository userRepo) {
        this.client = client;
        this.connection = stream;
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void run() {
        try {
            while (true) {
                connection.startReading();

                MessageTypes msgType = MessageTypes.lookup(connection.readByte());
                if (msgType == null) {
                    connection.finishReading();
                    continue;
                }

                switch(msgType) {
                    case JOINED_CHATROOM:
                        long jcChatroomId = connection.readLong();
                        long jcUserId = connection.readLong();
                        String jcHandle = connection.readString();
                        connection.finishReading();

                        User jcUser = getOrCreateUser(jcUserId, jcHandle);
                        Chatroom jcChatroom = chatroomRepo.get(jcChatroomId);
                        client.onJoinedChatroom(jcChatroom, jcUser);
                        break;

                    case LEFT_CHATROOM:
                        long lcChatroomId = connection.readLong();
                        long lcUserId = connection.readLong();
                        connection.finishReading();

                        User lcUser = userRepo.get(lcUserId, null).get().getUser();
                        Chatroom lcChatroom = chatroomRepo.get(lcChatroomId);
                        client.onLeftChatroom(lcChatroom, lcUser);
                        break;

                    case CHATROOM:
                        long chatroomId = connection.readLong();
                        long ownerId = connection.readLong();
                        String chatroomName = connection.readString();
                        String ownerName = connection.readString();
                        connection.finishReading();

                        User owner = getOrCreateUser(ownerId, ownerName);
                        Chatroom chatroom = getOrCreateChatroom(chatroomId, chatroomName, owner);

                        client.onChatroom(chatroom);
                        break;

                    case MESSAGE:
                        long msgID = connection.readLong();
                        long millis = connection.readLong();
                        long userId = connection.readLong();
                        long chatId = connection.readLong();
                        String userName = connection.readString();
                        String message = connection.readString();
                        connection.finishReading();

                        User sender = getOrCreateUser(userId, userName);
                        Chatroom chat = chatroomRepo.get(chatId);
                        Message msg = new Message(msgID, chat, sender, message, millis);

                        client.onMessage(msg);
                        break;

                    default:
                        System.err.println("Ignoring unhandled message: " + msgType);
                        connection.finishReading();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ExecutionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            System.exit(0);
        }
    }

    private Chatroom getOrCreateChatroom(long chatroomId, String chatroomName, User owner) {
        Chatroom chatroom;
        synchronized (chatroomRepo) {
            chatroom = chatroomRepo.get(chatroomId);
            if (chatroom == null) {
                chatroom = new Chatroom(chatroomId, chatroomName, owner);
                chatroomRepo.addChatroom(chatroom);
            }
        }
        return chatroom;
    }

    private User getOrCreateUser(long userId, String userName) throws ExecutionException, InterruptedException, InvalidObjectException {
        User owner;
        synchronized (userRepo) {
            owner = userRepo.get(userId, null).get().getUser();
            if (owner == null) {
                owner = new User(userId, userName, "", userName);
                userRepo.addUser(owner);
            }
        }
        return owner;
    }
}
