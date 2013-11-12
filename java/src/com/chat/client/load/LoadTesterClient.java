package com.chat.client.load;

import com.chat.ChatMessage;
import com.chat.Chatroom;
import com.chat.ChatroomRepository;
import com.chat.User;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientConnection;
import com.chat.client.ChatClientDispatcher;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.*;
import com.chat.select.EventService;
import com.chat.select.impl.EventServiceImpl;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class LoadTesterClient implements ChatClient {
    private final String username;
    private final String password;
    private final ChatClientConnection connection;

    private long chatroomId = 1;
    private long userId;

    private LoginState state = LoginState.None;

    private int messagesRecv = 0;
    private int sentMessages = 0;

    public LoadTesterClient(EventService eventService, String host, int port, String user, String password, ChatroomRepository chatroomRepo, InMemoryUserRepository userRepo) throws IOException {
        this.username = user;
        this.password = password;

        ChatClientDispatcher dispatcher = new ChatClientDispatcher(this, chatroomRepo, userRepo);
        connection = new ChatClientConnection(user, eventService, host, port, dispatcher);
    }

    @Override
    public void onChatroom(Chatroom chatroom) throws IOException {

    }

    @Override
    public void onMessage(ChatMessage message) {
        messagesRecv++;
    }

    public void sendMessage(String message) throws IOException {
        sentMessages++;
        connection.sendMessage(new SubmitMessageMessage(userId, chatroomId, message));
    }

    @Override
    public void onConnectAccept(int apiVersion, long globalChatId) {
        state = LoginState.ConnectAccept;
        connection.sendMessage(new RegisterMessage(username, password, username, username));
    }

    @Override
    public void onConnectReject(String reason) {
        state = LoginState.Error;
        System.err.println("connectReject: " + reason);
        System.exit(0);
    }

    @Override
    public void onRegisterAccept(long userId) {
        state = LoginState.RegisterAcceptOrReject;
        connection.sendMessage(new LoginMessage(username, password));
    }

    @Override
    public void onRegisterReject(String reason) {
        state = LoginState.RegisterAcceptOrReject;
        connection.sendMessage(new LoginMessage(username, password));
    }

    @Override
    public void onLoginAccept(long userId) {
        state = LoginState.LoginAccept;
        this.userId = userId;
        connection.sendMessage(new JoinChatroomMessage(userId, 1, 0, 0));
    }

    @Override
    public void onLoginReject(String reason) {
        state = LoginState.Error;
        System.err.println("Login reject: " + reason);
        System.exit(0);
    }

    @Override
    public void onJoinedChatroom(Chatroom chat, User user) {
        if (user.getId() == userId && chat.getId() == chatroomId) {
            state = LoginState.JoinedChatroom;
        }
    }

    @Override
    public void onLeftChatroom(Chatroom chatroom, User user) {

    }

    @Override
    public void onJoinedChatroomReject(String reason) {
        System.err.println("joinedChatroomReject: " + reason);
        state = LoginState.Error;
    }

    public LoginState getState() {
        return state;
    }

    public boolean isAlive() {
        return connection.isConnected() && state == LoginState.JoinedChatroom;
    }

    public int getMessagesRecv() {
        return messagesRecv;
    }

    public enum LoginState {
        None,
        ConnectAccept,
        RegisterAcceptOrReject,
        LoginAccept,
        JoinedChatroom,
        Error
    }
}
