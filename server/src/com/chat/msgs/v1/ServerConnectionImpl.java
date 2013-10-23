package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.MessageTypes;

import java.io.IOException;

import static com.chat.Utilities.*;

public class ServerConnectionImpl implements ServerConnection {
    private final BinaryStream stream;
    private String uuid;

    public ServerConnectionImpl(BinaryStream stream) {
       this.stream = stream;
    }

    @Override
    public String getUUID() {
       return uuid;
    }

    @Override
    public void setUUID(String UUID) {
       this.uuid = UUID;
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
    public void recvUnknown() throws IOException {
        stream.finishReading();
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
        String reason = stream.readString();  
        stream.finishReading();
                
        return new LoginAcceptMessage(reason);
    }

    @Override
    public LoginRejectMessage recvLoginReject() throws IOException {
        int APIVersion = stream.readInt();  
        String UUID = stream.readString();  
        stream.finishReading();
                
        return new LoginRejectMessage(APIVersion, UUID);
    }

    @Override
    public ConnectMessage recvConnect() throws IOException {
        int APIVersion = stream.readInt();  
        String UUID = stream.readString();  
        stream.finishReading();
                
        return new ConnectMessage(APIVersion, UUID);
    }

    @Override
    public ConnectAcceptMessage recvConnectAccept() throws IOException {
        int APIVersion = stream.readInt();  
        long globalChatId = stream.readLong();  
        stream.finishReading();
                
        return new ConnectAcceptMessage(APIVersion, globalChatId);
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
    public JoinChatroomFailureMessage recvJoinChatroomFailure() throws IOException {
        long chatroomId = stream.readLong();  
        String reason = stream.readString();  
        stream.finishReading();
                
        return new JoinChatroomFailureMessage(chatroomId, reason);
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
    public CreateChatroomFailureMessage recvCreateChatroomFailure() throws IOException {
        String chatroomName = stream.readString();  
        String reason = stream.readString();  
        stream.finishReading();
                
        return new CreateChatroomFailureMessage(chatroomName, reason);
    }
    
    @Override
    public void sendRegister(RegisterMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + getStrLen(msg.getUserName()) + getStrLen(msg.getPassword()) + getStrLen(msg.getHandle()));
            stream.writeByte(1);
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
            stream.writeByte(11);
            stream.writeString(msg.getUserName());
            stream.writeString(msg.getPassword());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendSubmitMessage(SubmitMessageMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + 8 + getStrLen(msg.getMessage()));
            stream.writeByte(21);
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
            stream.writeByte(31);
            stream.writeLong(msg.getLatitude());
            stream.writeLong(msg.getLongitude());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendJoinChatroom(JoinChatroomMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + 8 + 8 + 8);
            stream.writeByte(33);
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
            stream.writeByte(34);
            stream.writeLong(msg.getUserId());
            stream.writeLong(msg.getChatroomId());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendCreateChatroom(CreateChatroomMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + getStrLen(msg.getChatroomName()) + 8 + 8 + 8);
            stream.writeByte(35);
            stream.writeLong(msg.getOwnerId());
            stream.writeString(msg.getChatroomName());
            stream.writeLong(msg.getLatitude());
            stream.writeLong(msg.getLongitude());
            stream.writeLong(msg.getRadius());
            stream.finishWriting();
        }
    }
}
