package com.chat.msgs;

import com.chat.*;
import com.chat.msgs.v1.*;
import com.chat.server.ChatServer;
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

    @Override
    public void run() {
        // nothing
    }

    @Override
    public void runOnce(BinaryStream stream) throws IOException, ValidationError, ExecutionException, InterruptedException {
        byte typeByte = stream.readByte();
        MessageTypes type = MessageTypes.lookup(typeByte);

        if (type == null) {
            throw new ValidationError("Unknown message type: " + (int)typeByte);
        }

        switch (type) {
            case SearchChatrooms:
                SearchChatroomsMessage scMsg = new SearchChatroomsMessage(stream);
                server.searchChatrooms(stream);
                break;

            case CreateChatroom:
                CreateChatroomMessage ccMsg = new CreateChatroomMessage(stream);
                User ccUser = getAndValidateUser(ccMsg.getOwnerId());
                server.createChatroom(stream, ccUser, ccMsg.getChatroomName());
                break;

            case JoinChatroom:
                JoinChatroomMessage jcMsg = new JoinChatroomMessage(stream);
                User jcUser = getAndValidateUser(jcMsg.getUserId());
                Chatroom jcChatroom = getAndValidateChatroom(jcMsg.getChatroomId());
                server.joinChatroom(stream, jcUser, jcChatroom);
                break;

            case LeaveChatroom:
                LeaveChatroomMessage lcMsg = new LeaveChatroomMessage(stream);
                User lcUser = getAndValidateUser(lcMsg.getUserId());
                Chatroom lcChatroom = getAndValidateChatroom(lcMsg.getChatroomId());
                server.leaveChatroom(stream, lcUser, lcChatroom, false);
                break;

            case Register:
                RegisterMessage rMsg = new RegisterMessage(stream);
                server.registerUser(stream, rMsg.getUserName(), rMsg.getPassword(), rMsg.getHandle());
                break;

            case Login:
                LoginMessage lMsg = new LoginMessage(stream);
                server.login(stream, lMsg.getUserName(), lMsg.getPassword());
                break;

            case SubmitMessage:
                SubmitMessageMessage smMsg = new SubmitMessageMessage(stream);
                User user = getAndValidateUser(smMsg.getUserId());
                Chatroom chatroom = getAndValidateChatroom(smMsg.getChatroomId());
                log.debug("Sending {}", smMsg.getMessage());
                server.newMessage(stream, user, chatroom, smMsg.getMessage());
                break;

            default:
                throw new ValidationError("Unhandled message: " + type);
        }
    }

    private User getAndValidateUser(long userId) throws ValidationError, ExecutionException, InterruptedException {
        User user = userRepo.get(userId, null).get().getUser();

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
