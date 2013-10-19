package com.chat.client;

import com.chat.Chatroom;
import com.chat.MessageTypes;
import com.chat.Utilities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommandLineChatClient implements ChatClient {
    private final Socket socket;
    private final ClientConnection utilities;
    private final DataOutputStream dout;

    private Map<Long, Chatroom> chatroomIdToChatroom = new HashMap<Long, Chatroom>();
    private long subscribedChatroom;

    public CommandLineChatClient(String host, int port, String user, String password) throws IOException, InterruptedException {
        socket = new Socket(host, port);

        System.out.println("Connected to " + socket);

        DataInputStream din = new DataInputStream(socket.getInputStream());
        dout = new DataOutputStream(socket.getOutputStream());

        utilities = new ClientConnection(din, dout);
        utilities.registerAndLogin(user, password);
        utilities.searchChatrooms();

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatClientInput(this));
        pool.submit(new ChatClientListener(this, din));
    }

    @Override
    public void onChatroom(long chatroomId, String chatroomName, long ownerUserId, String ownerName) throws IOException {
        System.out.println("New chatroom: " + chatroomName + " by " + ownerName);

        Chatroom chatroom = new Chatroom();
        chatroom.id = chatroomId;
        chatroom.name = chatroomName;
        chatroomIdToChatroom.put(chatroomId, chatroom);

        // Subscribe to the first one!
        if (chatroomName.equalsIgnoreCase("Global")) {
            dout.writeShort(1 + 8 + 8);
            dout.writeByte(MessageTypes.JOIN_CHATROOM.getValue());
            dout.writeLong(utilities.getUserId());
            dout.writeLong(chatroomId);

            subscribedChatroom = chatroomId;
        }
    }

    @Override
    public void onMessage(String userName, String message) {
        System.out.println(userName + ": " + message);
    }

    @Override
    public void sendMessage(String message) throws IOException {
        dout.writeShort(1 + 8 + Utilities.getStringLength(message));
        dout.writeByte(MessageTypes.SUBMIT_MESSAGE.getValue());
        dout.writeLong(utilities.getUserId());
        dout.writeLong(subscribedChatroom);
        dout.writeUTF(message);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(args[1]);
        new CommandLineChatClient(args[0], port, args[2], args[3]);
    }
}
