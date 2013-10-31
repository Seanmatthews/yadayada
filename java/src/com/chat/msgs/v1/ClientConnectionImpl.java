package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.msgs.ValidationError;

import java.io.IOException;

import static com.chat.msgs.Utilities.*;

public class ClientConnectionImpl implements ClientConnection {
    private final BinaryStream stream;
    private final String uuid;
    private final int apiVersion;

    public ClientConnectionImpl(BinaryStream stream, String UUID, int APIVersion) {
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
    public RegisterMessage recvRegister() throws IOException {
        String userName = stream.readString();  
        String password = stream.readString();  
        String handle = stream.readString();  
        stream.finishReading();
                
        return new RegisterMessage(userName, password, handle);
    }

    @Override
    public LoginMessage recvLogin() throws IOException {
        String userName = stream.readString();  
        String password = stream.readString();  
        stream.finishReading();
                
        return new LoginMessage(userName, password);
    }

    @Override
    public ConnectMessage recvConnect() throws IOException {
        int APIVersion = stream.readInt();  
        String UUID = stream.readString();  
        stream.finishReading();
                
        return new ConnectMessage(APIVersion, UUID);
    }

    @Override
    public SubmitMessageMessage recvSubmitMessage() throws IOException {
        long userId = stream.readLong();  
        long chatroomId = stream.readLong();  
        String message = stream.readString();  
        stream.finishReading();
                
        return new SubmitMessageMessage(userId, chatroomId, message);
    }

    @Override
    public SearchChatroomsMessage recvSearchChatrooms() throws IOException {
        long latitude = stream.readLong();  
        long longitude = stream.readLong();  
        stream.finishReading();
                
        return new SearchChatroomsMessage(latitude, longitude);
    }

    @Override
    public JoinChatroomMessage recvJoinChatroom() throws IOException {
        long userId = stream.readLong();  
        long chatroomId = stream.readLong();  
        long latitude = stream.readLong();  
        long longitude = stream.readLong();  
        stream.finishReading();
                
        return new JoinChatroomMessage(userId, chatroomId, latitude, longitude);
    }

    @Override
    public LeaveChatroomMessage recvLeaveChatroom() throws IOException {
        long userId = stream.readLong();  
        long chatroomId = stream.readLong();  
        stream.finishReading();
                
        return new LeaveChatroomMessage(userId, chatroomId);
    }

    @Override
    public CreateChatroomMessage recvCreateChatroom() throws IOException {
        long ownerId = stream.readLong();  
        String chatroomName = stream.readString();  
        long latitude = stream.readLong();  
        long longitude = stream.readLong();  
        long radius = stream.readLong();  
        stream.finishReading();
                
        return new CreateChatroomMessage(ownerId, chatroomName, latitude, longitude, radius);
    }
    
    @Override
    public void sendRegisterAccept(RegisterAcceptMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendRegisterReject(RegisterRejectMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendLoginAccept(LoginAcceptMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendLoginReject(LoginRejectMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendConnectAccept(ConnectAcceptMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendConnectReject(ConnectRejectMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendMessage(MessageMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendChatroom(ChatroomMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendJoinChatroomReject(JoinChatroomRejectMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendJoinedChatroom(JoinedChatroomMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendLeftChatroom(LeftChatroomMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
    
    @Override
    public void sendCreateChatroomReject(CreateChatroomRejectMessage msg) throws IOException {
        stream.queueMessage(msg);
    }
}
