package com.chat.client;

import com.chat.*;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 10:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChatClientListener implements Runnable {
    private final ChatClient client;
    private final DataInputStream din;
    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;

    public ChatClientListener(ChatClient client,
                              DataInputStream stream,
                              ChatroomRepository chatroomRepo,
                              UserRepository userRepo) {
        this.client = client;
        this.din = stream;
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void run() {
        while (true) {
            try {
                short size = din.readShort();

                byte messageType = din.readByte();
                MessageTypes types = MessageTypes.lookup(messageType);
                if (types == null) {
                    System.err.println("Unknown message type " + (int) messageType);
                }

                switch(types) {
                    case CHATROOM:
                        long chatroomId = din.readLong();
                        String chatroomName = din.readUTF();
                        long ownerId = din.readLong();
                        String ownerName = din.readUTF();

                        User owner = getOrCreateUser(ownerId, ownerName);

                        Chatroom chatroom;
                        synchronized (chatroomRepo) {
                            chatroom = chatroomRepo.get(chatroomId);
                            if (chatroom == null) {
                                chatroom = new Chatroom();
                                chatroom.id = chatroomId;
                                chatroom.name = chatroomName;
                                chatroom.owner = owner;
                            }
                        }

                        client.onChatroom(chatroom);

                        break;

                    case MESSAGE:
                        long msgID = din.readLong();
                        long userId = din.readLong();
                        long chatId = din.readLong();
                        String userName = din.readUTF();
                        String message = din.readUTF();

                        User user = getOrCreateUser(userId, userName);
                        Chatroom chat = chatroomRepo.get(chatId);
                        Message msg = new Message();
                        msg.id = msgID;
                        msg.sender = user;
                        msg.chatroom = chat;
                        msg.message = message;

                        client.onMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    private User getOrCreateUser(long userId, String userName) {
        User owner;
        synchronized (userRepo) {
            owner = userRepo.get(userId);
            if (owner == null) {
                owner = new User();
                owner.id = userId;
                owner.login = userName;
                userRepo.addUser(owner);
            }
        }
        return owner;
    }
}
