package com.chat.server;

import com.chat.*;
import com.chat.msgs.v1.*;
import com.chat.select.EventService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
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
    private final Logger log = LogManager.getLogger();

    private final EventService eventService;
    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;

    private final Map<User, ClientConnection> userConnectionMap = new HashMap<>();
    private final Map<ClientConnection, User> connectionUserMap = new HashMap<>();

    public ChatServerImpl(EventService eventService, UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) {
        this.eventService = eventService;
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
    }

    @Override
    public void removeConnection(ClientConnection sender) {
        User user = connectionUserMap.remove(sender);

        if (user != null) {
            log.debug("Removing connection user {} {}", sender, user);

            ClientConnection conn = userConnectionMap.remove(user);

            Iterator<Chatroom> chatrooms = user.getChatrooms();
            while(chatrooms.hasNext()) {
                Chatroom next = chatrooms.next();
                leaveChatroom(sender, user, next, true);

                if (conn != null && conn != user) {
                    leaveChatroom(conn, user, next, true);
                }
            }
        }
        else {
            log.debug("Removing connection {}", sender);
        }

        sender.close();
    }

    @Override
    public void connect(ClientConnection sender, int apiVersion, String uuid) {
        try {
            sender.sendMessage(new ConnectAcceptMessage(apiVersion, 1, "", ""));
        } catch (IOException e) {
            removeConnection(sender);
        }
    }

    @Override
    public void newMessage(ClientConnection senderConnection, User sender, Chatroom chatroom, String message) {
        try {
            if (message.length() == 0 || message.length() > ChatMessage.MAX_LENGTH) {
                senderConnection.sendMessage(new SubmitMessageRejectMessage(sender.getId(), chatroom.getId(), "Invalid message length: " + message.length()));
                return;
            }

            if (!chatroomRepo.containsUser(chatroom, sender)) {
                senderConnection.sendMessage(new SubmitMessageRejectMessage(sender.getId(), chatroom.getId(), "Not in chatroom: " + chatroom.getName()));
                return;
            }
        }
        catch(IOException e) {
            removeConnection(senderConnection);
        }

        log.debug("New message {} {}", sender, message);

        ChatMessage msg = messageRepo.create(chatroom, sender, message);
        //chatroom.addMessage(msg);

        MessageMessage msgToSend = new MessageMessage(msg.getId(), msg.getTimestamp(), msg.getSender().getId(), msg.getChatroom().getId(), msg.getSender().getHandle(), msg.getMessage());

        Iterator<User> chatUsers = chatroom.getUsers();
        while (chatUsers.hasNext()) {
            User user = chatUsers.next();

            ClientConnection connection = userConnectionMap.get(user);

            if (connection != null) {
                try {
                     connection.sendMessage(msgToSend);
                } catch (IOException e) {
                    removeConnection(connection);
                }
            }
        }
    }

    @Override
    public void createChatroom(ClientConnection senderConnection, User sender, String name) {
        log.debug("Creating chatroom {} by {}", name, sender);

        Chatroom chatroom = chatroomRepo.createChatroom(sender, name);

        try {
            sendChatroom(senderConnection, chatroom);
        } catch (IOException e) {
            removeConnection(senderConnection);
        }
    }

    private void sendChatroom(ClientConnection senderConnection, Chatroom chatroom) throws IOException {
        senderConnection.sendMessage(new ChatroomMessage(chatroom.getId(), chatroom.getOwner().getId(), chatroom.getName(), chatroom.getOwner().getHandle(), 0, 0, 0));
    }

    @Override
    public void registerUser(final ClientConnection senderConnection, final String login, String password, String handle, String UUID) {
        log.debug("Registering user {}", login);

        try {
            if (login.length() == 0) {
                senderConnection.sendMessage(new RegisterRejectMessage("Invalid login"));
                return;
            }
            if (password.length() == 0) {
                senderConnection.sendMessage(new RegisterRejectMessage("Invalid password"));
                return;
            }
            if (handle.length() ==0) {
                senderConnection.sendMessage(new RegisterRejectMessage("Invalid handle"));
                return;
            }
        } catch (IOException e) {
            removeConnection(senderConnection);
        }

        userRepo.registerUser(login, password, handle, UUID, new UserRepositoryCompletionHandler() {
            @Override
            public void onCompletion(final UserRepositoryActionResult result) {
                    eventService.addThreadedEvent(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                switch (result.getCode()) {
                                    case OK:
                                        senderConnection.sendMessage(new RegisterAcceptMessage(result.getUser().getId()));
                                        break;
                                    case ConnectionError:
                                        senderConnection.sendMessage(new RegisterRejectMessage("Connection error"));
                                        break;
                                    case UserAlreadyExists:
                                    case InvalidUserNameOrPassword:
                                    default:
                                        senderConnection.sendMessage(new RegisterRejectMessage(result.getMessage()));
                                        break;
                                }
                            } catch (IOException e) {
                                removeConnection(senderConnection);
                            }
                        }
                    });
            }
        });
    }

    @Override
    public void login(final ClientConnection senderConnection, final String login, String password) {
        log.debug("Logging in user {}", login);

        try {
            if (login == null || login.length() == 0) {
                senderConnection.sendMessage(new LoginRejectMessage("Invalid login"));
                return;
            }
            if (password == null || password.length() == 0) {
                senderConnection.sendMessage(new LoginRejectMessage("Invalid password"));
                return;
            }
        } catch (IOException e) {
            removeConnection(senderConnection);
        }

        userRepo.login(login, password, new UserRepositoryCompletionHandler() {
            @Override
            public void onCompletion(final UserRepositoryActionResult result) {
                eventService.addThreadedEvent(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            switch (result.getCode()) {
                                case OK:
                                    final User user = result.getUser();
                                    senderConnection.sendMessage(new LoginAcceptMessage(user.getId()));
                                    userConnectionMap.put(user, senderConnection);
                                    connectionUserMap.put(senderConnection, user);
                                    break;
                                case ConnectionError:
                                    senderConnection.sendMessage(new LoginRejectMessage("Connection error"));
                                    break;
                                case InvalidUserNameOrPassword:
                                default:
                                    senderConnection.sendMessage(new LoginRejectMessage(result.getMessage()));
                                    break;
                            }
                        } catch (IOException e) {
                            removeConnection(senderConnection);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void searchChatrooms(ClientConnection senderConnection) {
        log.debug("Searching chatrooms {}", senderConnection);

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
    public void joinChatroom(ClientConnection senderConnection, User sender, Chatroom chatroom) {
        log.debug("Adding {} to {}", sender, chatroom);

        if (chatroom.containsUser(sender)) {
            try {
                senderConnection.sendMessage(new JoinChatroomRejectMessage(chatroom.getId(), sender + " is already in " + chatroom));
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
                senderConnection.sendMessage(new JoinedChatroomMessage(chatroom.getId(), chatMember.getId(), chatMember.getHandle()));
            } catch (IOException e) {
                removeConnection(senderConnection);
            }

            // notify other user about me joining chat
            ClientConnection chatMemberSender = userConnectionMap.get(chatMember);
            if (chatMemberSender != null) {
                try {
                    chatMemberSender.sendMessage(meJoining);
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
            senderConnection.sendMessage(meJoining);
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
    public void leaveChatroom(ClientConnection senderConnection, User sender, Chatroom chatroom, boolean removing) {
        log.debug("Removing {} to {}", sender, chatroom);

        LeftChatroomMessage meLeaving = new LeftChatroomMessage(chatroom.getId(), sender.getId());

        Iterator<User> users = chatroom.getUsers();
        while(users.hasNext()) {
            // notify me about other user joining chat
            User chatMember = users.next();

            // notify other user about me joining chat
            ClientConnection chatMemberSender = userConnectionMap.get(chatMember);
            if (chatMemberSender != null) {
                try {
                    chatMemberSender.sendMessage(meLeaving);
                } catch (IOException e) {
                    if (!removing)
                        removeConnection(chatMemberSender);
                }
            }
        }

        // Now remove our user
        chatroom.removeUser(sender);
        sender.removeFromChatroom(chatroom);
    }
}
