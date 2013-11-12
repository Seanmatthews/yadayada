package com.chat.msgs;

import com.chat.*;
import com.chat.msgs.v1.*;
import com.chat.server.ChatServer;
import com.chat.util.buffer.ReadBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class V1Dispatcher implements MessageDispatcher {
    public static final int VERSION_ID = 1;

    private final Logger log = LogManager.getLogger();

    protected final ChatServer server;

    private final UserRepository userRepo;
    private final ChatroomRepository chatroomRepo;

    public V1Dispatcher(ChatServer server, UserRepository userRepo, ChatroomRepository chatroomRepo) {
        this.server = server;
        this.userRepo = userRepo;
        this.chatroomRepo = chatroomRepo;
    }

    public void onMessage(ClientConnection stream, ReadBuffer buffer) throws ValidationError {
        byte typeByte = buffer.readByte();
        MessageTypes type = MessageTypes.lookup(typeByte);

        if (type == null) {
            throw new ValidationError("Unknown message type: " + (int)typeByte);
        }

        switch (type) {
            case Connect:
                ConnectMessage cMsg = new ConnectMessage(buffer);
                logMsg(cMsg);
                server.connect(stream, cMsg.getAPIVersion(), cMsg.getUUID());
                break;

            case SearchChatrooms:
                SearchChatroomsMessage scMsg = new SearchChatroomsMessage(buffer);
                logMsg(scMsg);
                server.searchChatrooms(stream);
                break;

            case CreateChatroom:
                CreateChatroomMessage ccMsg = new CreateChatroomMessage(buffer);
                logMsg(ccMsg);
                User ccUser = getAndValidateUser(ccMsg.getOwnerId());
                server.createChatroom(stream, ccUser, ccMsg.getChatroomName());
                break;

            case JoinChatroom:
                JoinChatroomMessage jcMsg = new JoinChatroomMessage(buffer);
                logMsg(jcMsg);
                User jcUser = getAndValidateUser(jcMsg.getUserId());
                Chatroom jcChatroom = getAndValidateChatroom(jcMsg.getChatroomId());
                server.joinChatroom(stream, jcUser, jcChatroom);
                break;

            case LeaveChatroom:
                LeaveChatroomMessage lcMsg = new LeaveChatroomMessage(buffer);
                logMsg(lcMsg);
                User lcUser = getAndValidateUser(lcMsg.getUserId());
                Chatroom lcChatroom = getAndValidateChatroom(lcMsg.getChatroomId());
                server.leaveChatroom(stream, lcUser, lcChatroom);
                break;

            case Register:
                RegisterMessage rMsg = new RegisterMessage(buffer);
                logMsg(rMsg);
                server.registerUser(stream, rMsg.getUserName(), rMsg.getPassword(), rMsg.getHandle(), rMsg.getUUID());
                break;

            case Login:
                LoginMessage lMsg = new LoginMessage(buffer);
                logMsg(lMsg);
                server.login(stream, lMsg.getUserName(), lMsg.getPassword());
                break;

            case QuickLogin:
                QuickLoginMessage qlMsg = new QuickLoginMessage(buffer);
                logMsg(qlMsg);
                server.quickLogin(stream, qlMsg.getHandle(), qlMsg.getUUID());
                break;

            case SubmitMessage:
                SubmitMessageMessage smMsg = new SubmitMessageMessage(buffer);
                logMsg(smMsg);
                User user = getAndValidateUser(smMsg.getUserId());
                Chatroom chatroom = getAndValidateChatroom(smMsg.getChatroomId());
                server.newMessage(stream, user, chatroom, smMsg.getMessage());
                break;

            default:
                throw new ValidationError("Unhandled message: " + type);
        }
    }

    private void logMsg(Message msg) {
        if (log.isDebugEnabled()) {
            log.debug(msg.toString());
        }
    }

    private User getAndValidateUser(long userId) throws ValidationError {
        User user;

        try {
            user = userRepo.get(userId, null).get().getUser();
        } catch (InterruptedException | ExecutionException e) {
            throw new ValidationError("Unknown user: " + userId);
        }

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
}
