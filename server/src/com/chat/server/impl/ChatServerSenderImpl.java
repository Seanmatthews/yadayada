package com.chat.server.impl;

import com.chat.*;
import com.chat.server.ChatClientSender;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/20/13
 * Time: 9:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatServerSenderImpl implements ChatClientSender {
    private final Connection connection;

    public ChatServerSenderImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void sendRegisterAccept(User user) throws IOException {
        synchronized (connection) {
            connection.writeShort(1 + 8);
            connection.writeByte(MessageTypes.REGISTER_ACCEPT.getValue());
            connection.writeLong(user.getId());
        }
    }

    @Override
    public void sendRegisterReject(String reason) throws IOException {
        synchronized (connection) {
            connection.writeShort(1 + Utilities.getStringLength(reason));
            connection.writeByte(MessageTypes.REGISTER_REJECT.getValue());
            connection.writeString(reason);
        }
    }

    @Override
    public void sendLoginAccept(User user) throws IOException {
        synchronized (connection) {
            connection.writeShort(1 + 8);
            connection.writeByte(MessageTypes.LOGIN_ACCEPT.getValue());
            connection.writeLong(user.getId());
        }
    }

    @Override
    public void sendLoginReject(String reason) throws IOException {
        synchronized (connection) {
            connection.writeShort(1 + Utilities.getStringLength(reason));
            connection.writeByte(MessageTypes.LOGIN_REJECT.getValue());
            connection.writeString(reason);
        }
    }

    @Override
    public void sendMessage(Message msg) throws IOException {
        synchronized (connection) {
            System.out.println("Sending message " + msg);

            connection.writeShort(1 + 8 + 8 + Utilities.getStringLength(msg.getSender().getHandle()) + Utilities.getStringLength(msg.getMessage()));
            connection.writeByte(MessageTypes.MESSAGE.getValue());
            connection.writeLong(msg.getId());
            connection.writeLong(msg.getTimestamp());
            connection.writeLong(msg.getSender().getId());
            connection.writeLong(msg.getChatroom().getId());
            connection.writeString(msg.getSender().getHandle());
            connection.writeString(msg.getMessage());
        }
    }

    @Override
    public void sendChatroom(Chatroom chatroom) throws IOException {
        synchronized (connection) {
            System.out.println("Send chatroom " + chatroom);

            connection.writeShort(1 + 8 + Utilities.getStringLength(chatroom.getName()) + 8 + Utilities.getStringLength(chatroom.getOwner().getHandle()));
            connection.writeByte(MessageTypes.CHATROOM.getValue());
            connection.writeLong(chatroom.getId());
            connection.writeLong(chatroom.getOwner().getId());
            connection.writeString(chatroom.getName());
            connection.writeString(chatroom.getOwner().getHandle());
        }
    }

    @Override
    public void sendJoinChatroomReject(Chatroom chatroom, String reason) throws IOException {
        synchronized (connection) {
            connection.writeShort(1 + 8 + Utilities.getStringLength(reason));
            connection.writeByte(MessageTypes.JOIN_CHATROOM_REJECT.getValue());
            connection.writeLong(chatroom.getId());
            connection.writeString(reason);
        }
    }

    @Override
    public void sendJoinedChatroom(Chatroom chatroom, User user) throws IOException {
        synchronized (connection) {
            connection.writeShort(1 + 8 + 8 + Utilities.getStringLength(user.getHandle()));
            connection.writeByte(MessageTypes.JOINED_CHATROOM.getValue());
            connection.writeLong(chatroom.getId());
            connection.writeLong(user.getId());
            connection.writeString(user.getHandle());
        }
    }

    @Override
    public void sendLeftChatroom(Chatroom chatroom, User user) throws IOException {
        synchronized (connection) {
            connection.writeShort(1 + 8 + 8 + Utilities.getStringLength(user.getHandle()));
            connection.writeByte(MessageTypes.LEFT_CHATROOM.getValue());
            connection.writeLong(chatroom.getId());
            connection.writeLong(user.getId());
        }
    }

    @Override
    public void close() {
        synchronized (connection) {
            connection.close();
        }
    }

    @Override
    public String toString() {
        return connection.toString();
    }
}
