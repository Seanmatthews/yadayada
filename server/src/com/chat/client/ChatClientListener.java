package com.chat.client;

import com.chat.*;

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

    public ChatClientListener(ChatClient client, DataInputStream stream, ChatroomRepository chatroomRepo, UserRepository userRepo) {
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

                if (size <= 0) {
                    System.err.println("Bad size: " + size);
                    System.exit(0);
                }

                byte messageType = din.readByte();
                MessageTypes types = MessageTypes.lookup(messageType);
                if (types == null) {
                    System.err.println("Unknown message type " + (int) messageType);

                    // get rid of remaining
                    din.read(new byte[size - 1]);
                    continue;
                }

                switch(types) {
                    case JOINED_CHATROOM:
                        long jcChatroomId = din.readLong();
                        long jcUserId = din.readLong();
                        String jcUserName = din.readUTF();

                        User jcUser = getOrCreateUser(jcUserId, jcUserName);
                        Chatroom jcChatroom = chatroomRepo.get(jcChatroomId);
                        client.onJoinedChatroom(jcChatroom, jcUser);
                        break;

                    case CHATROOM:
                        long chatroomId = din.readLong();
                        String chatroomName = din.readUTF();
                        long ownerId = din.readLong();
                        String ownerName = din.readUTF();

                        User owner = getOrCreateUser(ownerId, ownerName);
                        Chatroom chatroom = getOrCreateChatroom(chatroomId, chatroomName, owner);

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
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    private Chatroom getOrCreateChatroom(long chatroomId, String chatroomName, User owner) {
        Chatroom chatroom;
        synchronized (chatroomRepo) {
            chatroom = chatroomRepo.get(chatroomId);
            if (chatroom == null) {
                chatroom = new Chatroom();
                chatroom.id = chatroomId;
                chatroom.name = chatroomName;
                chatroom.owner = owner;
                chatroomRepo.addChatroom(chatroom);
            }
        }
        return chatroom;
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
