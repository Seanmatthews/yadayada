package com.chat.server.impl;

import com.chat.*;
import com.chat.server.ClientConnection;
import com.chat.server.ChatServer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.chat.UserRepository.*;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatServerImpl implements ChatServer {
    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;

    private final Map<String, ClientConnection> uuidConnectionMap = new ConcurrentHashMap<>();
    private final Map<User, ClientConnection> userConnectionMap = new ConcurrentHashMap<>();
    private final Map<ClientConnection, User> connectionUserMap = new ConcurrentHashMap<>();

    public ChatServerImpl(UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) {
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
    }

    @Override
    public void removeConnection(ClientConnection sender) {
        System.out.println("Removing connection to " + sender);

        User user = connectionUserMap.remove(sender);

        if (user != null) {
            userConnectionMap.remove(user);

            Iterator<Chatroom> chatrooms = user.getChatrooms();
            while(chatrooms.hasNext()) {
                leaveChatroom(sender, user, chatrooms.next());
            }
        }

        if (sender.getUUID() != null) {
            uuidConnectionMap.remove(sender.getUUID());
        }

        sender.close();
    }

    @Override
    public void connect(ClientConnection senderConnection, int apiVersion, String uuid) {
        ClientConnection connection = uuidConnectionMap.remove(uuid);
        if (connection != null) {
            System.out.println("Removing an old connection that was not cleaned up");
            removeConnection(connection);
        }

        try {
            uuidConnectionMap.put(uuid, senderConnection);

            senderConnection.sendConnectAccept(1, uuid, 1);
        } catch (IOException e) {
            removeConnection(senderConnection);
        }
    }

    @Override
    public void mapClientConnectionToUser(ClientConnection senderConnection, User user) {
        userConnectionMap.put(user, senderConnection);
        connectionUserMap.put(senderConnection, user);
    }

    @Override
    public void newMessage(ClientConnection senderConnection, User sender, Chatroom chatroom, String message) {
        System.out.println("New message from " + sender + " " + message);

        Message msg = messageRepo.create(chatroom, sender, message);
        chatroom.addMessage(msg);

        Iterator<User> chatUsers = chatroom.getUsers();
        while (chatUsers.hasNext()) {
            User user = chatUsers.next();
            ClientConnection connection = userConnectionMap.get(user);

            if (connection != null) {
                try {
                     connection.sendMessage(msg);
                } catch (IOException e) {
                    removeConnection(connection);
                }
            }
        }
    }

    @Override
    public void createChatroom(ClientConnection senderConnection, User sender, String name) {
        System.out.println("Creating chatroom " + name + " by " + sender);

        Chatroom chatroom = chatroomRepo.createChatroom(sender, name);

        try {
            senderConnection.sendChatroom(chatroom);
        } catch (IOException e) {
            removeConnection(senderConnection);
        }
    }

    @Override
    public void registerUser(final ClientConnection senderConnection, final String login, String password, String handle) {
        System.out.println("Registering user " + login);

        userRepo.registerUser(login, password, handle, senderConnection.getUUID(), new UserRepositoryCompletionHandler() {
            @Override
            public void onCompletion(UserRepositoryActionResult result) {
                try {
                    switch (result.getCode()) {
                        case OK:
                            senderConnection.sendRegisterAccept(result.getUser());
                            break;
                        case ConnectionError:
                            senderConnection.sendRegisterReject("BinaryStream error");
                            break;
                        case UserAlreadyExists:
                        case InvalidUserNameOrPassword:
                        default:
                            senderConnection.sendRegisterReject(result.getMessage());
                            break;
                    }
                } catch (IOException e) {
                    removeConnection(senderConnection);
                }
            }
        });
    }

    @Override
    public void quickRegisterUser(final ClientConnection senderConnection, String handle) {
        System.out.println("Quick registering user " + handle);

        userRepo.quickRegisterUser(handle, senderConnection.getUUID(), new UserRepositoryCompletionHandler() {
            @Override
            public void onCompletion(UserRepositoryActionResult result) {
                try {
                    switch (result.getCode()) {
                        case OK:
                            senderConnection.sendRegisterAccept(result.getUser());
                            break;
                        case ConnectionError:
                            senderConnection.sendRegisterReject("BinaryStream error");
                            break;
                        case UserAlreadyExists:
                        default:
                            senderConnection.sendRegisterReject(result.getMessage());
                            break;
                    }
                } catch (IOException e) {
                    removeConnection(senderConnection);
                }
            }
        });
    }

    @Override
    public void login(final ClientConnection senderConnection, final String login, String password) {
        System.out.println("Logging in user " + login);

        userRepo.login(login, password, new UserRepositoryCompletionHandler() {
            @Override
            public void onCompletion(UserRepositoryActionResult result) {
                try {
                    switch (result.getCode()) {
                        case OK:
                            User user = result.getUser();
                            senderConnection.sendLoginAccept(user);
                            userConnectionMap.put(user, senderConnection);
                            connectionUserMap.put(senderConnection, user);
                            break;
                        case ConnectionError:
                            senderConnection.sendLoginReject("BinaryStream error");
                            break;
                        case InvalidUserNameOrPassword:
                        default:
                            senderConnection.sendLoginReject(result.getMessage());
                            break;
                    }
                } catch (IOException e) {
                    removeConnection(senderConnection);
                }
            }
        });
    }

    @Override
    public void searchChatrooms(ClientConnection senderConnection) {
        System.out.println("Searching chatrooms " + senderConnection);

        Iterator<Chatroom> chatrooms = chatroomRepo.search(new ChatroomSearchCriteria());

        while(chatrooms.hasNext()) {
            try {
                senderConnection.sendChatroom(chatrooms.next());
            } catch (IOException e) {
                removeConnection(senderConnection);
            }
        }
    }

    @Override
    public void joinChatroom(ClientConnection senderConnection, User sender, Chatroom chatroom) {
        System.out.println("Adding " + sender + " to " + chatroom);

        if (chatroom.containsUser(sender)) {
            try {
                senderConnection.sendJoinChatroomReject(chatroom, sender + " is already in " + chatroom);
            } catch (IOException e) {
                removeConnection(senderConnection);
            }
            return;
        }

        Iterator<User> users = chatroom.getUsers();
        while(users.hasNext()) {
            // notify me about other user joining chat
            User chatMember = users.next();
            try {
                senderConnection.sendJoinedChatroom(chatroom, chatMember);
            } catch (IOException e) {
                removeConnection(senderConnection);
            }

            // notify other user about me joining chat
            ClientConnection chatMemberSender = userConnectionMap.get(chatMember);
            if (chatMemberSender != null) {
                try {
                    chatMemberSender.sendJoinedChatroom(chatroom, sender);
                } catch (IOException e) {
                    removeConnection(chatMemberSender);
                }
            }
        }

        // Now add our user
        chatroom.addUser(sender);
        sender.addToChatroom(chatroom);

        try {
            // Give me confirmation that I've joined the chat
            senderConnection.sendJoinedChatroom(chatroom, sender);
        } catch (IOException e) {
            removeConnection(senderConnection);
        }

        // send the new entrant the last N messages
        Iterator<Message> recentMessages = chatroom.getRecentMessages();
        while(recentMessages.hasNext()) {
            try {
                senderConnection.sendMessage(recentMessages.next());
            } catch (IOException e) {
                removeConnection(senderConnection);
            }
        }
    }

    @Override
    public void leaveChatroom(ClientConnection senderConnection, User sender, Chatroom chatroom) {
        System.out.println("Removing " + sender + " from " + chatroom);

        Iterator<User> users = chatroom.getUsers();
        while(users.hasNext()) {
            // notify me about other user joining chat
            User chatMember = users.next();

            // notify other user about me joining chat
            ClientConnection chatMemberSender = userConnectionMap.get(chatMember);
            if (chatMemberSender != null) {
                try {
                    chatMemberSender.sendLeftChatroom(chatroom, sender);
                } catch (IOException e) {
                    removeConnection(chatMemberSender);
                }
            }
        }

        // Now remove our user
        chatroom.removeUser(sender);
        sender.removeFromChatroom(chatroom);
    }
}
