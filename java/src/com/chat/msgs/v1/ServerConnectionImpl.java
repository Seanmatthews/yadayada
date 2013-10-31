package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.ValidationError;

import java.io.IOException;

import static com.chat.msgs.Utilities.*;

public class ServerConnectionImpl implements ServerConnection {
    private final BinaryStream stream;
    private final String uuid;
    private final int apiVersion;

    public ServerConnectionImpl(BinaryStream stream, String UUID, int APIVersion) {
       this.stream = stream; 
       this.uuid = UUID;
       this.apiVersion = APIVersion;
    }

    @Override
    public String getUUID() {
       return uuid;
    }

    @Override
    public int getAPIVersion() {
        return apiVersion;
    }

    @Override 
    public MessageTypes recvMsgType() throws IOException, ValidationError {
        stream.startReading();
        byte msgTypeByte = stream.readByte();
        MessageTypes msgType = MessageTypes.lookup(msgTypeByte);

        if (msgType == null)
            throw new ValidationError("Unknown message type: " + (int)msgTypeByte);

        return msgType;        
    }

    @Override
    public void close() {
        stream.close();
    }

    @Override
    public String toString() {
        return stream.toString();
    }

    @Override
    public RegisterAcceptMessage recvRegisterAccept() throws IOException {
        long userId = stream.readLong();  
        stream.finishReading();
                
        return new RegisterAcceptMessage(userId);
    }

    @Override
    public RegisterRejectMessage recvRegisterReject() throws IOException {
        String reason = stream.readString();  
        stream.finishReading();
                
        return new RegisterRejectMessage(reason);
    }

    @Override
    public LoginAcceptMessage recvLoginAccept() throws IOException {
        long userId = stream.readLong();  
        stream.finishReading();
                
        return new LoginAcceptMessage(userId);
    }

    @Override
    public LoginRejectMessage recvLoginReject() throws IOException {
        String reason = stream.readString();  
        stream.finishReading();
                
        return new LoginRejectMessage(reason);
    }

    @Override
    public ConnectAcceptMessage recvConnectAccept() throws IOException {
        int APIVersion = stream.readInt();  
        long globalChatId = stream.readLong();  
        String imageUploadUrl = stream.readString();  
        String imageDownloadUrl = stream.readString();  
        stream.finishReading();
                
        return new ConnectAcceptMessage(APIVersion, globalChatId, imageUploadUrl, imageDownloadUrl);
    }

    @Override
    public ConnectRejectMessage recvConnectReject() throws IOException {
        String reason = stream.readString();  
        stream.finishReading();
                
        return new ConnectRejectMessage(reason);
    }

    @Override
    public MessageMessage recvMessage() throws IOException {
        long messageId = stream.readLong();  
        long messageTimestamp = stream.readLong();  
        long senderId = stream.readLong();  
        long chatroomId = stream.readLong();  
        String senderHandle = stream.readString();  
        String message = stream.readString();  
        stream.finishReading();
                
        return new MessageMessage(messageId, messageTimestamp, senderId, chatroomId, senderHandle, message);
    }

    @Override
    public ChatroomMessage recvChatroom() throws IOException {
        long chatroomId = stream.readLong();  
        long chatroomOwnerId = stream.readLong();  
        String chatroomName = stream.readString();  
        String chatroomOwnerHandle = stream.readString();  
        long latitude = stream.readLong();  
        long longitude = stream.readLong();  
        long radius = stream.readLong();  
        stream.finishReading();
                
        return new ChatroomMessage(chatroomId, chatroomOwnerId, chatroomName, chatroomOwnerHandle, latitude, longitude, radius);
    }

    @Override
    public JoinChatroomRejectMessage recvJoinChatroomReject() throws IOException {
        long chatroomId = stream.readLong();  
        String reason = stream.readString();  
        stream.finishReading();
                
        return new JoinChatroomRejectMessage(chatroomId, reason);
    }

    @Override
    public JoinedChatroomMessage recvJoinedChatroom() throws IOException {
        long chatroomId = stream.readLong();  
        long userId = stream.readLong();  
        String userHandle = stream.readString();  
        stream.finishReading();
                
        return new JoinedChatroomMessage(chatroomId, userId, userHandle);
    }

    @Override
    public LeftChatroomMessage recvLeftChatroom() throws IOException {
        long chatroomId = stream.readLong();  
        long userId = stream.readLong();  
        stream.finishReading();
                
        return new LeftChatroomMessage(chatroomId, userId);
    }

    @Override
    public CreateChatroomRejectMessage recvCreateChatroomReject() throws IOException {
        String chatroomName = stream.readString();  
        String reason = stream.readString();  
        stream.finishReading();
                
        return new CreateChatroomRejectMessage(chatroomName, reason);
    }
    
    @Override
    public void sendRegister(RegisterMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendLogin(LoginMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendConnect(ConnectMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendSubmitMessage(SubmitMessageMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendSearchChatrooms(SearchChatroomsMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendJoinChatroom(JoinChatroomMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendLeaveChatroom(LeaveChatroomMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendCreateChatroom(CreateChatroomMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
}
