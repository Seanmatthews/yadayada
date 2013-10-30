package com.chat.msgs.v1;

import com.chat.BinaryStream;

import java.io.IOException;

import static com.chat.Utilities.*;

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
    public MessageTypes recvMsgType() throws IOException {
        stream.startReading();
        byte msgTypeByte = stream.readByte();
        MessageTypes msgType = MessageTypes.lookup(msgTypeByte);

        if (msgType == null)
            throw new IOException("Unknown message type: " + (int)msgTypeByte);

        return msgType;        
    }

    @Override
    public void close() {
        synchronized (stream) {
            stream.close();
        }
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
        synchronized(stream) {
            stream.startWriting(1 + getStrLen(msg.getUserName()) + getStrLen(msg.getPassword()) + getStrLen(msg.getHandle()));
            stream.writeByte(MessageTypes.Register.getValue());
            stream.writeString(msg.getUserName());
            stream.writeString(msg.getPassword());
            stream.writeString(msg.getHandle());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendLogin(LoginMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + getStrLen(msg.getUserName()) + getStrLen(msg.getPassword()));
            stream.writeByte(MessageTypes.Login.getValue());
            stream.writeString(msg.getUserName());
            stream.writeString(msg.getPassword());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendConnect(ConnectMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 4 + getStrLen(msg.getUUID()));
            stream.writeByte(MessageTypes.Connect.getValue());
            stream.writeInt(msg.getAPIVersion());
            stream.writeString(msg.getUUID());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendSubmitMessage(SubmitMessageMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + 8 + getStrLen(msg.getMessage()));
            stream.writeByte(MessageTypes.SubmitMessage.getValue());
            stream.writeLong(msg.getUserId());
            stream.writeLong(msg.getChatroomId());
            stream.writeString(msg.getMessage());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendSearchChatrooms(SearchChatroomsMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + 8);
            stream.writeByte(MessageTypes.SearchChatrooms.getValue());
            stream.writeLong(msg.getLatitude());
            stream.writeLong(msg.getLongitude());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendJoinChatroom(JoinChatroomMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + 8 + 8 + 8);
            stream.writeByte(MessageTypes.JoinChatroom.getValue());
            stream.writeLong(msg.getUserId());
            stream.writeLong(msg.getChatroomId());
            stream.writeLong(msg.getLatitude());
            stream.writeLong(msg.getLongitude());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendLeaveChatroom(LeaveChatroomMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + 8);
            stream.writeByte(MessageTypes.LeaveChatroom.getValue());
            stream.writeLong(msg.getUserId());
            stream.writeLong(msg.getChatroomId());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendCreateChatroom(CreateChatroomMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + getStrLen(msg.getChatroomName()) + 8 + 8 + 8);
            stream.writeByte(MessageTypes.CreateChatroom.getValue());
            stream.writeLong(msg.getOwnerId());
            stream.writeString(msg.getChatroomName());
            stream.writeLong(msg.getLatitude());
            stream.writeLong(msg.getLongitude());
            stream.writeLong(msg.getRadius());
            stream.finishWriting();
        }
    }
}
