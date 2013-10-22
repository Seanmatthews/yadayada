package com.chat.server.impl;

import com.chat.*;
import com.chat.server.ClientConnection;
import com.chat.msgs.v1.*;

import java.io.IOException;

import static com.chat.Utilities.*;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/20/13
 * Time: 9:41 PM
 * To change this template use File | Settings | File Templates.
 */
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
    public MessageTypes recvMsgType() throws IOException {
        stream.startReading();
        byte msgTypeByte = stream.readByte();
        MessageTypes msgType = MessageTypes.lookup(msgTypeByte);

        if (msgType == null)
            throw new IOException("Unknown message type: " + (int)msgTypeByte);

        return msgType;
    }

    @Override
    public ConnectMessage recvConnect() throws IOException {
        int apiVersion = stream.readInt();
        String uuid = stream.readString();
        stream.finishReading();

        return new ConnectMessage(apiVersion, uuid);
    }

    @Override
    public void recvUnknown() throws IOException {
        stream.finishReading();
    }

    @Override
    public SearchChatroomsMessage recvSearchChatrooms() throws IOException {
        stream.finishReading();

        return new SearchChatroomsMessage();
    }

    @Override
    public CreateChatroomMessage recvCreateChatroom() throws IOException {
        long userId = stream.readLong();
        String chatroomName = stream.readString();
        stream.finishReading();

        return new CreateChatroomMessage(userId, chatroomName);
    }

    @Override
    public JoinChatroomMessage recvJoinChatroom() throws IOException {
        long userId = stream.readLong();
        long chatroomId = stream.readLong();
        stream.finishReading();

        return new JoinChatroomMessage(userId, chatroomId);
    }

    @Override
    public LeaveChatroomMessage recvLeaveChatroom() throws IOException {
        long userId = stream.readLong();
        long chatroomId = stream.readLong();
        stream.finishReading();

        return new LeaveChatroomMessage(userId, chatroomId);
    }

    @Override
    public RegisterMessage recvRegister() throws IOException {
        String login = stream.readString();
        String password = stream.readString();
        String handle = stream.readString();
        stream.finishReading();

        return new RegisterMessage(login, password, handle);
    }

    @Override
    public LoginMessage recvLogin() throws IOException {
        String login = stream.readString();
        String password = stream.readString();
        stream.finishReading();

        return new LoginMessage(login, password);
    }

    @Override
    public QuickRegisterMessage recvQuickRegister() throws IOException {
        String qrHandle = stream.readString();
        stream.finishReading();

        return new QuickRegisterMessage(qrHandle);
    }

    @Override
    public SubmitMessageMessage recvSubmitMessage() throws IOException {
        long userId = stream.readLong();
        long chatroomId = stream.readLong();
        String msg = stream.readString();
        stream.finishReading();

        return new SubmitMessageMessage(userId, chatroomId, msg);
    }

    @Override
    public void sendConnectionAccept(int apiVersion, String uuid, long globalChatId) throws IOException {
        synchronized (stream) {
            this.uuid = uuid;

            stream.startWriting(1 + 4 + 8);
            stream.writeByte(MessageTypes.CONNECT_ACCEPT.getValue());
            stream.writeInt(apiVersion);
            stream.writeLong(globalChatId);
            stream.finishWriting();
        }
    }

    @Override
    public void sendConnectionReject(String reason) throws IOException {
        synchronized (stream) {
            stream.startWriting(1 + getStrLen(reason));
            stream.writeByte(MessageTypes.CONNECT_REJECT.getValue());
            stream.writeString(reason);
            stream.finishWriting();
        }
    }

    @Override
    public void sendRegisterAccept(User user) throws IOException {
        synchronized (stream) {
            stream.startWriting(1 + 8);
            stream.writeByte(MessageTypes.REGISTER_ACCEPT.getValue());
            stream.writeLong(user.getId());
            stream.finishWriting();
        }
    }

    @Override
    public void sendRegisterReject(String reason) throws IOException {
        synchronized (stream) {
            stream.startWriting(1 + getStrLen(reason));
            stream.writeByte(MessageTypes.REGISTER_REJECT.getValue());
            stream.writeString(reason);
            stream.finishWriting();
        }
    }

    @Override
    public void sendLoginAccept(User user) throws IOException {
        synchronized (stream) {
            stream.startWriting(1 + 8);
            stream.writeByte(MessageTypes.LOGIN_ACCEPT.getValue());
            stream.writeLong(user.getId());
            stream.finishWriting();
        }
    }

    @Override
    public void sendLoginReject(String reason) throws IOException {
        synchronized (stream) {
            stream.startWriting(1 + getStrLen(reason));
            stream.writeByte(MessageTypes.LOGIN_REJECT.getValue());
            stream.writeString(reason);
            stream.finishWriting();
        }
    }

    @Override
    public void sendMessage(Message msg) throws IOException {
        synchronized (stream) {
            System.out.println("Sending message " + msg);

            String handle = msg.getSender().getHandle();
            String message = msg.getMessage();

            stream.startWriting(1 + (4 * 8) + getStrLen(handle) + getStrLen(message));
            stream.writeByte(MessageTypes.MESSAGE.getValue());
            stream.writeLong(msg.getId());
            stream.writeLong(msg.getTimestamp());
            stream.writeLong(msg.getSender().getId());
            stream.writeLong(msg.getChatroom().getId());
            stream.writeString(handle);
            stream.writeString(message);
            stream.finishWriting();
        }
    }

    @Override
    public void sendChatroom(Chatroom chatroom) throws IOException {
        synchronized (stream) {
            System.out.println("Send chatroom " + chatroom);

            String chatroomName = chatroom.getName();
            String ownerHandle = chatroom.getOwner().getHandle();

            stream.startWriting((1 + (2 * 8) + getStrLen(chatroomName) + getStrLen(ownerHandle)));
            stream.writeByte(MessageTypes.CHATROOM.getValue());
            stream.writeLong(chatroom.getId());
            stream.writeLong(chatroom.getOwner().getId());
            stream.writeString(chatroomName);
            stream.writeString(ownerHandle);
            stream.finishWriting();
        }
    }

    @Override
    public void sendJoinChatroomReject(Chatroom chatroom, String reason) throws IOException {
        synchronized (stream) {
            stream.startWriting(1 + 8 + getStrLen(reason));
            stream.writeByte(MessageTypes.JOIN_CHATROOM_REJECT.getValue());
            stream.writeLong(chatroom.getId());
            stream.writeString(reason);
            stream.finishWriting();
        }
    }

    @Override
    public void sendJoinedChatroom(Chatroom chatroom, User user) throws IOException {
        synchronized (stream) {
            stream.startWriting(1 + 8 + 8 + getStrLen(user.getHandle()));
            stream.writeByte(MessageTypes.JOINED_CHATROOM.getValue());
            stream.writeLong(chatroom.getId());
            stream.writeLong(user.getId());
            stream.writeString(user.getHandle());
            stream.finishWriting();
        }
    }

    @Override
    public void sendLeftChatroom(Chatroom chatroom, User user) throws IOException {
        synchronized (stream) {
            stream.startWriting(1 + 8 + 8);
            stream.writeByte(MessageTypes.LEFT_CHATROOM.getValue());
            stream.writeLong(chatroom.getId());
            stream.writeLong(user.getId());
            stream.finishWriting();
        }
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
}
