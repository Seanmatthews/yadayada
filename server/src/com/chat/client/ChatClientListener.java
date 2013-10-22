package com.chat.client;

import com.chat.*;

import java.io.DataInputStream;
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
public class ChatClientListener implements Runnable {
    private final ChatClient client;
    private final Connection din;
    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;

    public ChatClientListener(ChatClient client, Connection stream, ChatroomRepository chatroomRepo, UserRepository userRepo) {
        this.client = client;
        this.din = stream;
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void run() {
        try {
            while (true) {
                short size = din.readShort();

                if (size <= 0) {
                    System.err.println("Bad size: " + size);
                    System.exit(0);
                }

                byte messageType = din.readByte();
                MessageTypes types = MessageTypes.lookup(messageType);
                if (types == null) {
                    System.err.println("Unknown message type " + (int) messageType);
                    System.exit(0);
                }

                switch(types) {
                    case JOINED_CHATROOM:
                        long jcChatroomId = din.readLong();
                        long jcUserId = din.readLong();
                        String jcHandle = din.readString();

                        User jcUser = getOrCreateUser(jcUserId, jcHandle);
                        Chatroom jcChatroom = chatroomRepo.get(jcChatroomId);
                        client.onJoinedChatroom(jcChatroom, jcUser);
                        break;

                    case LEFT_CHATROOM:
                        long lcChatroomId = din.readLong();
                        long lcUserId = din.readLong();

                        User lcUser = userRepo.get(lcUserId, null).get();
                        Chatroom lcChatroom = chatroomRepo.get(lcChatroomId);
                        client.onLeftChatroom(lcChatroom, lcUser);
                        break;

                    case CHATROOM:
                        long chatroomId = din.readLong();
                        long ownerId = din.readLong();
                        String chatroomName = din.readString();
                        String ownerName = din.readString();

                        User owner = getOrCreateUser(ownerId, ownerName);
                        Chatroom chatroom = getOrCreateChatroom(chatroomId, chatroomName, owner);

                        client.onChatroom(chatroom);
                        break;

                    case MESSAGE:
                        long msgID = din.readLong();
                        long millis = din.readLong();
                        long userId = din.readLong();
                        long chatId = din.readLong();
                        String userName = din.readString();
                        String message = din.readString();

                        User sender = getOrCreateUser(userId, userName);
                        Chatroom chat = chatroomRepo.get(chatId);
                        Message msg = new Message(msgID, chat, sender, message, millis);

                        client.onMessage(msg);
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
            owner = userRepo.get(userId, null).get();
            if (owner == null) {
                owner = new User(userId, userName, "", userName);
                userRepo.addUser(owner);
            }
        }
        return owner;
    }
}
