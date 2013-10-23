package com.chat.client;

import com.chat.*;
import com.chat.msgs.v1.*;

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
    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;
    private final ServerConnection connection;

    public ChatClientDispatcher(ChatClient client, BinaryStream stream, ChatroomRepository chatroomRepo, UserRepository userRepo) {
        this.client = client;
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
        this.connection = new ServerConnectionImpl(stream);
    }

    @Override
    public void run() {
        try {
            while (true) {
                MessageTypes msgType = connection.recvMsgType();

                switch(msgType) {
                    case JoinedChatroom:
                        JoinedChatroomMessage jcMsg = connection.recvJoinedChatroom();
                        User jcUser = getOrCreateUser(jcMsg.getUserId(), jcMsg.getUserHandle());
                        Chatroom jcChatroom = chatroomRepo.get(jcMsg.getChatroomId());
                        client.onJoinedChatroom(jcChatroom, jcUser);
                        break;

                    case LeftChatroom:
                        LeftChatroomMessage lcMsg = connection.recvLeftChatroom();
                        User lcUser = userRepo.get(lcMsg.getUserId(), null).get().getUser();
                        Chatroom lcChatroom = chatroomRepo.get(lcMsg.getChatroomId());
                        client.onLeftChatroom(lcChatroom, lcUser);
                        break;

                    case Chatroom:
                        ChatroomMessage cMsg = connection.recvChatroom();
                        User owner = getOrCreateUser(cMsg.getChatroomOwnerId(), cMsg.getChatroomOwnerHandle());
                        Chatroom chatroom = getOrCreateChatroom(cMsg.getChatroomId(), cMsg.getChatroomName(), owner);
                        client.onChatroom(chatroom);
                        break;

                    case Message:
                        MessageMessage msg = connection.recvMessage();
                        User sender = getOrCreateUser(msg.getSenderId(), msg.getSenderHandle());
                        Chatroom chat = chatroomRepo.get(msg.getChatroomId());
                        Message theMsg = new Message(msg.getMessageId(), chat, sender, msg.getMessage(), msg.getMessageTimestamp());
                        client.onMessage(theMsg);
                        break;

                    default:
                        System.err.println("Ignoring unhandled message: " + msgType);
                        connection.recvUnknown();
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
