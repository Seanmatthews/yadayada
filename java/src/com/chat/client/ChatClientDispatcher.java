package com.chat.client;

import com.chat.*;
import com.chat.impl.InMemoryUserRepository;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.*;
import com.chat.util.buffer.ReadBuffer;
import com.chat.util.tcp.TCPCrackerClientListener;
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
                    log.info("ConnectAccept APIVersion={} GlobalChatId={}", caMsg.getAPIVersion(), caMsg.getGlobalChatId());
                    break;

                case ConnectReject:
                    ConnectRejectMessage crMsg = new ConnectRejectMessage(buffer);
                    log.info("ConnectReject {}", crMsg.getReason());
                    System.exit(0);
                    break;

                case RegisterAccept:
                    RegisterAcceptMessage raMsg = new RegisterAcceptMessage(buffer);
                    log.info("RegisterAccept UserId={}", raMsg.getUserId());
                    break;

                case RegisterReject:
                    RegisterRejectMessage rrMsg = new RegisterRejectMessage(buffer);
                    log.info("RegisterReject {}", rrMsg.getReason());
                    break;

                case LoginAccept:
                    LoginAcceptMessage laMsg = new LoginAcceptMessage(buffer);
                    client.onLoginAccept(laMsg.getUserId());
                    break;

                case LoginReject:
                    LoginRejectMessage lrMsg = new LoginRejectMessage(buffer);
                    log.info("LoginReject {}" + lrMsg.getReason());
                    break;

                case JoinedChatroom:
                    JoinedChatroomMessage jcMsg = new JoinedChatroomMessage(buffer);
                    User jcUser = getOrCreateUser(jcMsg.getUserId(), jcMsg.getUserHandle());
                    Chatroom jcChatroom = chatroomRepo.get(jcMsg.getChatroomId());
                    client.onJoinedChatroom(jcChatroom, jcUser);
                    break;

                case JoinChatroomReject:
                    JoinChatroomRejectMessage jcrMsg = new JoinChatroomRejectMessage(buffer);
                    client.onJoinedChatroomReject(jcrMsg.getReason());
                    break;

                case LeftChatroom:
                    LeftChatroomMessage lcMsg = new LeftChatroomMessage(buffer);
                    User lcUser = userRepo.get(lcMsg.getUserId(), null).get().getUser();
                    Chatroom lcChatroom = chatroomRepo.get(lcMsg.getChatroomId());
                    client.onLeftChatroom(lcChatroom, lcUser);
                    break;

                case Chatroom:
                    ChatroomMessage cMsg = new ChatroomMessage(buffer);
                    User owner = getOrCreateUser(cMsg.getChatroomOwnerId(), cMsg.getChatroomOwnerHandle());
                    Chatroom chatroom = getOrCreateChatroom(cMsg.getChatroomId(), cMsg.getChatroomName(), owner);
                    client.onChatroom(chatroom);
                    break;

                case Message:
                    MessageMessage msg = new MessageMessage(buffer);
                    User sender = getOrCreateUser(msg.getSenderId(), msg.getSenderHandle());
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
                chatroom = new Chatroom(chatroomId, chatroomName, owner, chatroomRepo);
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
                owner = new User(userId, userName, "", userName, userRepo);
                userRepo.addUser(owner);
            }
        }
        return owner;
    }
}
