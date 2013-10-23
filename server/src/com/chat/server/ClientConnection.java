package com.chat.server;

import com.chat.Chatroom;
import com.chat.Message;
import com.chat.MessageTypes;
import com.chat.User;
import com.chat.msgs.v1.*;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/20/13
 * Time: 9:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ClientConnection {
    String getUUID();

    MessageTypes recvMsgType() throws IOException;
    ConnectMessage recvConnect() throws IOException;
    SearchChatroomsMessage recvSearchChatrooms() throws IOException;
    CreateChatroomMessage recvCreateChatroom() throws IOException;
    JoinChatroomMessage recvJoinChatroom() throws IOException;
    LeaveChatroomMessage recvLeaveChatroom() throws IOException;
    RegisterMessage recvRegister() throws IOException;
    LoginMessage recvLogin() throws IOException;
    SubmitMessageMessage recvSubmitMessage() throws IOException;
    void recvUnknown() throws IOException;

    void sendConnectAccept(int apiVersion, String uuid, long globalChatId) throws IOException;
    void sendConnectReject(String reason) throws IOException;
    void sendRegisterAccept(User user) throws IOException;
    void sendRegisterReject(String reason) throws IOException;
    void sendLoginAccept(User user) throws IOException;
    void sendLoginReject(String reason) throws IOException;
    void sendMessage(Message message) throws IOException;
    void sendChatroom(Chatroom chatroom) throws IOException;
    void sendJoinChatroomReject(Chatroom chatroom, String reason) throws IOException;
    void sendJoinedChatroom(Chatroom chatroom, User user) throws IOException;
    void sendLeftChatroom(Chatroom chatroom, User user) throws IOException;
    void close();
}
