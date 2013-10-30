package com.chat.msgs.v1;

import java.io.IOException;

public interface ServerConnection {
    String getUUID();
    int getAPIVersion();
    MessageTypes recvMsgType() throws IOException;
    void close();

    RegisterAcceptMessage recvRegisterAccept() throws IOException;
    RegisterRejectMessage recvRegisterReject() throws IOException;
    LoginAcceptMessage recvLoginAccept() throws IOException;
    LoginRejectMessage recvLoginReject() throws IOException;
    ConnectAcceptMessage recvConnectAccept() throws IOException;
    ConnectRejectMessage recvConnectReject() throws IOException;
    MessageMessage recvMessage() throws IOException;
    ChatroomMessage recvChatroom() throws IOException;
    JoinChatroomRejectMessage recvJoinChatroomReject() throws IOException;
    JoinedChatroomMessage recvJoinedChatroom() throws IOException;
    LeftChatroomMessage recvLeftChatroom() throws IOException;
    CreateChatroomRejectMessage recvCreateChatroomReject() throws IOException;
    
    void sendRegister(RegisterMessage msg) throws IOException;
    void sendLogin(LoginMessage msg) throws IOException;
    void sendConnect(ConnectMessage msg) throws IOException;
    void sendSubmitMessage(SubmitMessageMessage msg) throws IOException;
    void sendSearchChatrooms(SearchChatroomsMessage msg) throws IOException;
    void sendJoinChatroom(JoinChatroomMessage msg) throws IOException;
    void sendLeaveChatroom(LeaveChatroomMessage msg) throws IOException;
    void sendCreateChatroom(CreateChatroomMessage msg) throws IOException;
}
