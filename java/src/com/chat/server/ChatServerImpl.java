package com.chat.server;

import com.chat.*;
import com.chat.msgs.v1.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.chat.UserRepository.UserRepositoryActionResult;
import static com.chat.UserRepository.UserRepositoryCompletionHandler;

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

    private final Map<User, BinaryStream> userConnectionMap = new ConcurrentHashMap<>();
    private final Map<BinaryStream, User> connectionUserMap = new ConcurrentHashMap<>();

    public ChatServerImpl(UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) {
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
    }

    @Override
    public void addConnection(BinaryStream senderConnection) {
        try {
            senderConnection.sendMessage(new ConnectAcceptMessage(senderConnection.getAPIVersion(), 1, "", ""), true);
        } catch (IOException e) {
            removeConnection(senderConnection);
        }
    }

    @Override
    public void removeConnection(BinaryStream sender) {
        System.out.println("Removing connection to " + sender);

        User user = connectionUserMap.remove(sender);

        if (user != null) {
            userConnectionMap.remove(user);

            Iterator<Chatroom> chatrooms = user.getChatrooms();
            while(chatrooms.hasNext()) {
                leaveChatroom(sender, user, chatrooms.next());
            }
        }

        sender.close();
    }

    @Override
    public void newMessage(BinaryStream senderConnection, User sender, Chatroom chatroom, String message) {
        System.out.println("New message from " + sender + " " + message);

        ChatMessage msg = messageRepo.create(chatroom, sender, message);
        //chatroom.addMessage(msg);

        MessageMessage msgToSend = new MessageMessage(msg.getId(), msg.getTimestamp(), msg.getSender().getId(), msg.getChatroom().getId(), msg.getSender().getHandle(), msg.getMessage());

        Iterator<User> chatUsers = chatroom.getUsers();
        while (chatUsers.hasNext()) {
            User user = chatUsers.next();

            BinaryStream connection = userConnectionMap.get(user);

            if (connection != null) {
                try {
                     connection.sendMessage(msgToSend, true);
                } catch (IOException e) {
                    removeConnection(connection);
                }
            }
        }
    }

    @Override
    public void createChatroom(BinaryStream senderConnection, User sender, String name) {
        System.out.println("Creating chatroom " + name + " by " + sender);

        Chatroom chatroom = chatroomRepo.createChatroom(sender, name);

        try {
            sendChatroom(senderConnection, chatroom);
        } catch (IOException e) {
            removeConnection(senderConnection);
        }
    }

    private void sendChatroom(BinaryStream senderConnection, Chatroom chatroom) throws IOException {
        senderConnection.sendMessage(new ChatroomMessage(chatroom.getId(), chatroom.getOwner().getId(), chatroom.getName(), chatroom.getOwner().getHandle(), 0, 0, 0), true);
    }

    @Override
    public void registerUser(final BinaryStream senderConnection, final String login, String password, String handle) {
        System.out.println("Registering user " + login);

        try {
            if (login == null || login.length() == 0) {
                senderConnection.sendMessage(new RegisterRejectMessage("Invalid login"), true);
                return;
            }
            if (password == null || password.length() == 0) {
                senderConnection.sendMessage(new RegisterRejectMessage("Invalid password"), true);
                return;
            }
            if (handle == null || handle.length() ==0) {
                senderConnection.sendMessage(new RegisterRejectMessage("Invalid handle"), true);
                return;
            }
        } catch (IOException e) {
            removeConnection(senderConnection);
        }

        userRepo.registerUser(login, password, handle, senderConnection.getUUID(), new UserRepositoryCompletionHandler() {
            @Override
            public void onCompletion(UserRepositoryActionResult result) {
                try {
                    switch (result.getCode()) {
                        case OK:
                            senderConnection.sendMessage(new RegisterAcceptMessage(result.getUser().getId()), false);
                            break;
                        case ConnectionError:
                            senderConnection.sendMessage(new RegisterRejectMessage("Connection error"), false);
                            break;
                        case UserAlreadyExists:
                        case InvalidUserNameOrPassword:
                        default:
                            senderConnection.sendMessage(new RegisterRejectMessage(result.getMessage()), false);
                            break;
                    }
                } catch (IOException e) {
                    removeConnection(senderConnection);
                }
            }
        });
    }

    @Override
    public void login(final BinaryStream senderConnection, final String login, String password) {
        System.out.println("Logging in user " + login);

        try {
            if (login == null || login.length() == 0) {
                senderConnection.sendMessage(new LoginRejectMessage("Invalid login"), true);
                return;
            }
            if (password == null || password.length() == 0) {
                senderConnection.sendMessage(new LoginRejectMessage("Invalid password"), true);
                return;
            }
        } catch (IOException e) {
            removeConnection(senderConnection);
        }

        userRepo.login(login, password, new UserRepositoryCompletionHandler() {
            @Override
            public void onCompletion(UserRepositoryActionResult result) {
                try {
                    switch (result.getCode()) {
                        case OK:
                            User user = result.getUser();
                            senderConnection.sendMessage(new LoginAcceptMessage(user.getId()), false);
                            userConnectionMap.put(user, senderConnection);
                            connectionUserMap.put(senderConnection, user);
                            break;
                        case ConnectionError:
                            senderConnection.sendMessage(new LoginRejectMessage("Connection error"), false);
                            break;
                        case InvalidUserNameOrPassword:
                        default:
                            senderConnection.sendMessage(new LoginRejectMessage(result.getMessage()), false);
                            break;
                    }
                } catch (IOException e) {
                    removeConnection(senderConnection);
                }
            }
        });
    }

    @Override
    public void searchChatrooms(BinaryStream senderConnection) {
        System.out.println("Searching chatrooms " + senderConnection);

        Iterator<Chatroom> chatrooms = chatroomRepo.search(new ChatroomSearchCriteria());

        while(chatrooms.hasNext()) {
            try {
                sendChatroom(senderConnection, chatrooms.next());
            } catch (IOException e) {
                removeConnection(senderConnection);
            }
        }
    }

    @Override
    public void joinChatroom(BinaryStream senderConnection, User sender, Chatroom chatroom) {
        System.out.println("Adding " + sender + " to " + chatroom);

        if (chatroom.containsUser(sender)) {
            try {
                senderConnection.sendMessage(new JoinChatroomRejectMessage(chatroom.getId(), sender + " is already in " + chatroom), true);
            } catch (IOException e) {
                removeConnection(senderConnection);
            }
            return;
        }

        JoinedChatroomMessage meJoining = new JoinedChatroomMessage(chatroom.getId(), sender.getId(), sender.getHandle());

        Iterator<User> users = chatroomRepo.getUsers(chatroom);// chatroom.getUsers();
        while(users.hasNext()) {
            // notify me about other user joining chat
            User chatMember = users.next();
            try {
                senderConnection.sendMessage(new JoinedChatroomMessage(chatroom.getId(), chatMember.getId(), chatMember.getHandle()), true);
            } catch (IOException e) {
                removeConnection(senderConnection);
            }

            // notify other user about me joining chat
            BinaryStream chatMemberSender = userConnectionMap.get(chatMember);
            if (chatMemberSender != null) {
                try {
                    chatMemberSender.sendMessage(meJoining, true);
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
            senderConnection.sendMessage(meJoining, true);
        } catch (IOException e) {
            removeConnection(senderConnection);
        }

        // send the new entrant the last N messages
        /*Iterator<ChatMessage> recentMessages = chatroom.getRecentMessages();
        while(recentMessages.hasNext()) {
            try {
                ChatMessage msg = recentMessages.next();
                MessageMessage recentMessage = new MessageMessage(msg.getId(), msg.getTimestamp(), msg.getSender().getId(), msg.getChatroom().getId(), msg.getSender().getHandle(), msg.getMessage());
                senderConnection.sendMessage(recentMessage);
            } catch (IOException e) {
                removeConnection(senderConnection);
            }
        }*/
    }

    @Override
    public void leaveChatroom(BinaryStream senderConnection, User sender, Chatroom chatroom) {
        System.out.println("Removing " + sender + " from " + chatroom);

        LeftChatroomMessage meLeaving = new LeftChatroomMessage(chatroom.getId(), sender.getId());

        Iterator<User> users = chatroom.getUsers();
        while(users.hasNext()) {
            // notify me about other user joining chat
            User chatMember = users.next();

            // notify other user about me joining chat
            BinaryStream chatMemberSender = userConnectionMap.get(chatMember);
            if (chatMemberSender != null) {
                try {
                    chatMemberSender.sendMessage(meLeaving, true);
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
