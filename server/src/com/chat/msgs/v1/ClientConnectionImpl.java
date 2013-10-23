package com.chat.msgs.v1;

import com.chat.BinaryStream;
import com.chat.MessageTypes;

import java.io.IOException;

import static com.chat.Utilities.*;

public class ClientConnectionImpl implements ClientConnection {
    private final BinaryStream stream;
    private String uuid;

    public ClientConnectionImpl(BinaryStream stream) {
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
        synchronized(stream) {
            stream.startWriting(1 + 8);
            stream.writeByte(2);
            stream.writeLong(msg.getUserId());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendRegisterReject(RegisterRejectMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + getStrLen(msg.getReason()));
            stream.writeByte(3);
            stream.writeString(msg.getReason());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendLoginAccept(LoginAcceptMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + getStrLen(msg.getReason()));
            stream.writeByte(12);
            stream.writeString(msg.getReason());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendLoginReject(LoginRejectMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 4 + getStrLen(msg.getUUID()));
            stream.writeByte(13);
            stream.writeInt(msg.getAPIVersion());
            stream.writeString(msg.getUUID());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendConnect(ConnectMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 4 + getStrLen(msg.getUUID()));
            stream.writeByte(16);
            stream.writeInt(msg.getAPIVersion());
            stream.writeString(msg.getUUID());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendConnectAccept(ConnectAcceptMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 4 + 8);
            stream.writeByte(17);
            stream.writeInt(msg.getAPIVersion());
            stream.writeLong(msg.getGlobalChatId());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendConnectReject(ConnectRejectMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + getStrLen(msg.getReason()));
            stream.writeByte(18);
            stream.writeString(msg.getReason());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendMessage(MessageMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + 8 + 8 + 8 + getStrLen(msg.getSenderHandle()) + getStrLen(msg.getMessage()));
            stream.writeByte(22);
            stream.writeLong(msg.getMessageId());
            stream.writeLong(msg.getMessageTimestamp());
            stream.writeLong(msg.getSenderId());
            stream.writeLong(msg.getChatroomId());
            stream.writeString(msg.getSenderHandle());
            stream.writeString(msg.getMessage());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendChatroom(ChatroomMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + 8 + getStrLen(msg.getChatroomName()) + getStrLen(msg.getChatroomOwnerHandle()) + 8 + 8 + 8);
            stream.writeByte(32);
            stream.writeLong(msg.getChatroomId());
            stream.writeLong(msg.getChatroomOwnerId());
            stream.writeString(msg.getChatroomName());
            stream.writeString(msg.getChatroomOwnerHandle());
            stream.writeLong(msg.getLatitude());
            stream.writeLong(msg.getLongitude());
            stream.writeLong(msg.getRadius());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendJoinChatroomFailure(JoinChatroomFailureMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + getStrLen(msg.getReason()));
            stream.writeByte(36);
            stream.writeLong(msg.getChatroomId());
            stream.writeString(msg.getReason());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendJoinedChatroom(JoinedChatroomMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + 8 + getStrLen(msg.getUserHandle()));
            stream.writeByte(37);
            stream.writeLong(msg.getChatroomId());
            stream.writeLong(msg.getUserId());
            stream.writeString(msg.getUserHandle());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendLeftChatroom(LeftChatroomMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + 8 + 8);
            stream.writeByte(38);
            stream.writeLong(msg.getChatroomId());
            stream.writeLong(msg.getUserId());
            stream.finishWriting();
        }
    }
    
    @Override
    public void sendCreateChatroomFailure(CreateChatroomFailureMessage msg) throws IOException {
        synchronized(stream) {
            stream.startWriting(1 + getStrLen(msg.getChatroomName()) + getStrLen(msg.getReason()));
            stream.writeByte(38);
            stream.writeString(msg.getChatroomName());
            stream.writeString(msg.getReason());
            stream.finishWriting();
        }
    }
}
