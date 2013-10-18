package com.chat;

import com.chat.impl.ChatroomRepositoryImpl;
import com.chat.impl.UserRepositoryImpl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/16/13
 * Time: 9:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatServer {
    private final UserRepository userRepo;
    private final ChatroomRepository chatroomRepo;

    private final ExecutorService execService = Executors.newFixedThreadPool(100);
    private final Map<Socket, DataOutputStream> streams = new HashMap<Socket, DataOutputStream>();
    private final Map<User, DataOutputStream> userStreams = new HashMap<User, DataOutputStream>();
    private ServerSocket serverSocket;

    private long nextMessageId = 1;

    public ChatServer(int port, UserRepository userRepo, ChatroomRepository chatroomRepo) throws IOException {
        this.userRepo = userRepo;
        this.chatroomRepo = chatroomRepo;

        listen(port);
    }

    private void listen(int port) throws IOException {
        serverSocket = new ServerSocket(port);

        System.out.println("Listening on: " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Connection from: " + socket);

            DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
            streams.put(socket, stream);
            execService.submit(new ChatServerListener(this, socket, userRepo, chatroomRepo));
        }
    }

    public void newMessage(Socket socket, User sender, Chatroom chatroom, String message) {
        Message msg = new Message();
        msg.message = message;
        msg.chatroom = chatroom;
        msg.id = nextMessageId++;

        synchronized (userStreams) {
            Iterator<User> chatUsers = chatroom.getUsers();
            while (chatUsers.hasNext()) {
                User user = chatUsers.next();
                DataOutputStream dout = userStreams.get(user);
                if (dout != null) {
                    try {
                        dout.writeShort(1 + 8 + 8 + getLength(sender.login) + getLength(msg.message));
                        dout.writeByte(MessageTypes.MESSAGE.getValue());
                        dout.writeLong(msg.id);
                        dout.writeLong(chatroom.id);
                        dout.writeLong(sender.id);
                        dout.writeUTF(sender.login);
                        dout.writeUTF(msg.message);
                    } catch (IOException e) {
                        removeConnection(socket);
                    }
                }
            }
        }
    }

    public void removeConnection(Socket socket) {
        System.out.println("Removing connection to " + socket);

        synchronized (streams) {
            streams.remove(socket);

            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    public void createChatroom(Socket socket, User user, String name) {
        Chatroom chatroom = chatroomRepo.createChatroom(user, name);

        synchronized (streams) {
            DataOutputStream dout = streams.get(socket);
            sendChatroom(socket, dout, chatroom);
        }
    }

    public void registerUser(Socket socket, String login, String password) {
        User user = userRepo.registerUser(login, password);

        synchronized (streams) {
            DataOutputStream dout = streams.get(socket);

            try {
                if (user == null) {
                    String msg = "Registration failure. " + user.login + " already exists";
                    dout.writeShort(1 + getLength(msg));
                    dout.writeByte(MessageTypes.REGISTER_REJECT.getValue());
                    dout.writeUTF(msg);
                }
                else {
                    dout.writeShort(1 + 8);
                    dout.writeByte(MessageTypes.REGISTER_ACCEPT.getValue());
                    dout.writeLong(user.id);
                }
            } catch (IOException e) {
                System.out.println("Error writing to client when registering user");
                removeConnection(socket);
            }
        }
    }

    public void login(Socket socket, String login, String password) {
        User user = userRepo.login(login, password);

        synchronized (streams) {
            DataOutputStream dout = streams.get(socket);

            try {
                if (user == null) {
                    String msg = "Invalid user or password: " + login;
                    dout.writeShort(1 + getLength(msg));
                    dout.writeByte(MessageTypes.LOGIN_REJECT.getValue());
                    dout.writeUTF(msg);
                    return;
                }

                dout.writeShort(1 + 8);
                dout.writeByte(MessageTypes.LOGIN_ACCEPT.getValue());
                dout.writeLong(user.id);

                synchronized (userStreams) {
                    if (userStreams.containsKey(user)) {
                        DataOutputStream stream = userStreams.remove(user);
                        try {
                            stream.close();
                        } catch(Exception e) {
                            // do nothing
                        }
                    }
                    userStreams.put(user, dout);
                }
            } catch (IOException e) {
                removeConnection(socket);
            }
        }
    }

    public void searchChatrooms(Socket socket) {
        List<Chatroom> chatrooms = chatroomRepo.search(null);

        synchronized (streams) {
            DataOutputStream dout = streams.get(socket);

            for (Chatroom chatroom : chatrooms) {
                sendChatroom(socket, dout, chatroom);
            }
        }
    }

    private void sendChatroom(Socket socket, DataOutputStream dout, Chatroom chatroom) {
        try {
            int msgBytes = 1 + 8 + getLength(chatroom.name) + 8 + getLength(chatroom.owner.login);
            dout.writeShort(msgBytes);
            dout.write(MessageTypes.CHATROOM.getValue());
            dout.writeLong(chatroom.id);
            dout.writeUTF(chatroom.name);
            dout.writeLong(chatroom.owner.id);
            dout.writeUTF(chatroom.owner.login);
        } catch (IOException e) {
            removeConnection(socket);
        }
    }

    public void joinChatroom(Socket socket, User user, Chatroom chatroom) {
        System.out.println("Adding " + user.login + " to " + chatroom.name);
        chatroom.addUser(user);

        // TODO: send a response?
    }

    private static int getLength(String str) {
        return 2 + str.length();
    }
}
