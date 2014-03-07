package com.chat.client;

import com.chat.ChatMessage;
import com.chat.Chatroom;
import com.chat.ChatroomRepository;
import com.chat.User;
import com.chat.impl.InMemoryUserRepository;
import com.chat.msgs.Message;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.*;
import com.chat.util.buffer.ReadBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class ChatClientDispatcher {
    private final Logger log = LogManager.getLogger();

    private final ChatClient client;
    private final ChatroomRepository chatroomRepo;
    private final InMemoryUserRepository userRepo;

    public ChatClientDispatcher(ChatClient client, ChatroomRepository chatroomRepo, InMemoryUserRepository userRepo) {
        this.client = client;
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
    }

    public void onMessage(ReadBuffer buffer) {
        try {
            MessageTypes msgType = MessageTypes.lookup(buffer.readByte());

            switch(msgType) {
                case ConnectAccept:
                    ConnectAcceptMessage caMsg = new ConnectAcceptMessage(buffer);
                    logMsg(caMsg);
                    client.onConnectAccept(caMsg.getAPIVersion(), caMsg.getGlobalChatId());
                    break;

                case ConnectReject:
                    ConnectRejectMessage crMsg = new ConnectRejectMessage(buffer);
                    logMsg(crMsg);
                    client.onConnectReject(crMsg.getReason());
                    break;

                case RegisterAccept:
                    RegisterAcceptMessage raMsg = new RegisterAcceptMessage(buffer);
                    logMsg(raMsg);
                    client.onRegisterAccept(raMsg.getUserId());
                    break;

                case RegisterReject:
                    RegisterRejectMessage rrMsg = new RegisterRejectMessage(buffer);
                    logMsg(rrMsg);
                    client.onRegisterReject(rrMsg.getReason());
                    break;

                case LoginAccept:
                    LoginAcceptMessage laMsg = new LoginAcceptMessage(buffer);
                    logMsg(laMsg);
                    client.onLoginAccept(laMsg.getUserId());
                    break;

                case LoginReject:
                    LoginRejectMessage lrMsg = new LoginRejectMessage(buffer);
                    logMsg(lrMsg);
                    client.onLoginReject(lrMsg.getReason());
                    break;

                case JoinedChatroom:
                    JoinedChatroomMessage jcMsg = new JoinedChatroomMessage(buffer);
                    logMsg(jcMsg);
                    // Don't send user phone number with this message
                    User jcUser = getOrCreateUser(jcMsg.getUserId(), jcMsg.getUserHandle(), 0L);
                    Chatroom jcChatroom = getOrCreateChatroom(jcMsg.getChatroomId(), "Global", jcUser);
                    client.onJoinedChatroom(jcChatroom, jcUser);
                    break;

                case JoinChatroomReject:
                    JoinChatroomRejectMessage jcrMsg = new JoinChatroomRejectMessage(buffer);
                    logMsg(jcrMsg);
                    client.onJoinedChatroomReject(jcrMsg.getReason());
                    break;

                case LeftChatroom:
                    LeftChatroomMessage lcMsg = new LeftChatroomMessage(buffer);
                    logMsg(lcMsg);
                    User lcUser = userRepo.get(lcMsg.getUserId(), null).get().getUser();
                    Chatroom lcChatroom = chatroomRepo.get(lcMsg.getChatroomId());
                    client.onLeftChatroom(lcChatroom, lcUser);
                    break;

                case Chatroom:
                    ChatroomMessage cMsg = new ChatroomMessage(buffer);
                    logMsg(cMsg);
                    // Don't send user phone number with this message
                    User owner = getOrCreateUser(cMsg.getChatroomOwnerId(), cMsg.getChatroomOwnerHandle(), 0L);
                    Chatroom chatroom = getOrCreateChatroom(cMsg.getChatroomId(), cMsg.getChatroomName(), owner);
                    client.onChatroom(chatroom);
                    break;

                case Message:
                    MessageMessage msg = new MessageMessage(buffer);
                    logMsg(msg);
                    // Don't send user phone number with every message
                    User sender = getOrCreateUser(msg.getSenderId(), msg.getSenderHandle(), 0L);
                    Chatroom chat = chatroomRepo.get(msg.getChatroomId());
                    ChatMessage theMsg = new ChatMessage(msg.getMessageId(), chat, sender, msg.getMessage(), msg.getMessageTimestamp());
                    client.onMessage(theMsg);
                    break;

                default:
                    throw new ValidationError("Ignoring unhandled message: " + msgType);
            }
        } catch (IOException e) {
            log.error(e);
            System.exit(0);
        } catch (InterruptedException e) {
            log.error(e);
            System.exit(0);
        } catch (ExecutionException e) {
            log.error(e);
            System.exit(0);
        } catch (ValidationError validationError) {
            log.error(validationError.getMessage(), validationError);
            System.exit(0);
        }
    }

    private Chatroom getOrCreateChatroom(long chatroomId, String chatroomName, User owner) {
        Chatroom chatroom;
        synchronized (chatroomRepo) {
            chatroom = chatroomRepo.get(chatroomId);
            if (chatroom == null) {
                chatroom = new Chatroom(chatroomId, chatroomName, owner, chatroomRepo, 0, 0, 0, false);
                chatroomRepo.addChatroom(chatroom);
            }
        }
        return chatroom;
    }

    private User getOrCreateUser(long userId, String userName, long phoneNumber) throws ExecutionException, InterruptedException, InvalidObjectException {
        User owner;
        synchronized (userRepo) {
            owner = userRepo.get(userId, null).get().getUser();
            if (owner == null) {
                owner = new User(userId, userName, "", userName, phoneNumber, userRepo);
                userRepo.addUser(owner);
            }
        }
        return owner;
    }

    private void logMsg(Message msg) {
        if (log.isDebugEnabled()) {
            log.debug(msg.toString());
        }
    }
}
