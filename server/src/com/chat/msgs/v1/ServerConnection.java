package com.chat.msgs.v1;

import com.chat.MessageTypes;

import java.io.IOException;

public interface ServerConnection {
    String getUUID();
    void setUUID(String UUID);
    MessageTypes recvMsgType() throws IOException;
    void recvUnknown() throws IOException;
    void close();

    RegisterAcceptMessage recvRegisterAccept() throws IOException;
    RegisterRejectMessage recvRegisterReject() throws IOException;
    LoginAcceptMessage recvLoginAccept() throws IOException;
    LoginRejectMessage recvLoginReject() throws IOException;
    ConnectMessage recvConnect() throws IOException;
    ConnectAcceptMessage recvConnectAccept() throws IOException;
    ConnectRejectMessage recvConnectReject() throws IOException;
    MessageMessage recvMessage() throws IOException;
    ChatroomMessage recvChatroom() throws IOException;
    JoinChatroomFailureMessage recvJoinChatroomFailure() throws IOException;
    JoinedChatroomMessage recvJoinedChatroom() throws IOException;
    LeftChatroomMessage recvLeftChatroom() throws IOException;
    CreateChatroomFailureMessage recvCreateChatroomFailure() throws IOException;
    
    void sendRegister(RegisterMessage msg) throws IOException;
    void sendLogin(LoginMessage msg) throws IOException;
    void sendSubmitMessage(SubmitMessageMessage msg) throws IOException;
    void sendSearchChatrooms(SearchChatroomsMessage msg) throws IOException;
    void sendJoinChatroom(JoinChatroomMessage msg) throws IOException;
    void sendLeaveChatroom(LeaveChatroomMessage msg) throws IOException;
    void sendCreateChatroom(CreateChatroomMessage msg) throws IOException;
}
