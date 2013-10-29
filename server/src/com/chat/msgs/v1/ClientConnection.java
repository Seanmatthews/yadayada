package com.chat.msgs.v1;

import java.io.IOException;

public interface ClientConnection {
    String getUUID();
    void setUUID(String UUID);
    MessageTypes recvMsgType() throws IOException;
    void recvUnknown() throws IOException;
    void close();

    RegisterMessage recvRegister() throws IOException;
    LoginMessage recvLogin() throws IOException;
    ConnectMessage recvConnect() throws IOException;
    SubmitMessageMessage recvSubmitMessage() throws IOException;
    SearchChatroomsMessage recvSearchChatrooms() throws IOException;
    JoinChatroomMessage recvJoinChatroom() throws IOException;
    LeaveChatroomMessage recvLeaveChatroom() throws IOException;
    CreateChatroomMessage recvCreateChatroom() throws IOException;
    
    void sendRegisterAccept(RegisterAcceptMessage msg) throws IOException;
    void sendRegisterReject(RegisterRejectMessage msg) throws IOException;
    void sendLoginAccept(LoginAcceptMessage msg) throws IOException;
    void sendLoginReject(LoginRejectMessage msg) throws IOException;
    void sendConnectAccept(ConnectAcceptMessage msg) throws IOException;
    void sendConnectReject(ConnectRejectMessage msg) throws IOException;
    void sendMessage(MessageMessage msg) throws IOException;
    void sendChatroom(ChatroomMessage msg) throws IOException;
    void sendJoinChatroomReject(JoinChatroomRejectMessage msg) throws IOException;
    void sendJoinedChatroom(JoinedChatroomMessage msg) throws IOException;
    void sendLeftChatroom(LeftChatroomMessage msg) throws IOException;
    void sendCreateChatroomReject(CreateChatroomRejectMessage msg) throws IOException;
}
