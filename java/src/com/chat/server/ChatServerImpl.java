package com.chat.server;

import com.chat.*;
import com.chat.msgs.v1.*;
import com.chat.select.EventService;
import com.chat.server.cluster.ChatroomCluster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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

    public ChatServerImpl(EventService eventService, UserRepository userRepo, ChatroomRepository chatroomRepo, MessageRepository messageRepo) {
        this.eventService = eventService;
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
    }

    @Override
    public void disconnect(ClientConnection sender) {
        User user = sender.getUser();

        log.debug("Disconnect {}", sender);

        if (user != null) {
            sender.setUser(null);
            userConnectionMap.remove(user);

            log.debug("Removing connection {} {}", user, sender);

            Iterator<Chatroom> chatrooms = user.getChatrooms();

            while(chatrooms != null && chatrooms.hasNext()) {
                Chatroom chatroom = chatrooms.next();

                // Remove our user
                chatroom.removeUser(user);
                chatrooms.remove();
                //user.removeFromChatroom(chatroom);

                Iterator<User> users = chatroom.getUsers();

                while(users.hasNext()) {
                    User chatroomUser = users.next();
                    ClientConnection chatroomUserConnection = userConnectionMap.get(chatroomUser);

                    if (chatroomUserConnection != null)
                        leaveChatroom(chatroomUserConnection, user, chatroom);
                }
            }
        }
        else {
            log.debug("Removing connection {}", sender);
        }
    }

    @Override
    public void connect(ClientConnection sender, int apiVersion, String uuid) {
        sender.sendMessage(new ConnectAcceptMessage(apiVersion, 1, "", ""));
    }

    @Override
    public void newMessage(ClientConnection senderConnection, User sender, Chatroom chatroom, String message) {
        if (message.length() == 0 || message.length() > ChatMessage.MAX_LENGTH) {
            senderConnection.sendMessage(new SubmitMessageRejectMessage(sender.getId(), chatroom.getId(), "Invalid message length: " + message.length()));
            return;
        }

        if (!chatroomRepo.containsUser(chatroom, sender)) {
            senderConnection.sendMessage(new SubmitMessageRejectMessage(sender.getId(), chatroom.getId(), "Not in chatroom: " + chatroom.getName()));
            return;
        }

        ChatMessage msg = messageRepo.create(chatroom, sender, message);
        ChatroomCluster cluster = chatroom.addMessage(msg);

        MessageMessage msgToSend = new MessageMessage(msg.getId(), msg.getTimestamp(), msg.getSender().getId(), msg.getChatroom().getId(), msg.getSender().getHandle(), msg.getMessage());

        Iterator<User> chatUsers = cluster.getUsers();
        while (chatUsers.hasNext()) {
            User user = chatUsers.next();

            ClientConnection connection = userConnectionMap.get(user);

            if (connection != null)
                connection.sendMessage(msgToSend);
        }
    }

    @Override
    public void createChatroom(ClientConnection senderConnection, User sender, String name, long latitude, long longitude, long radius) {
        log.debug("Creating chatroom {} by {}", name, sender);

        Chatroom chatroom = chatroomRepo.createChatroom(sender, name, latitude, longitude, radius);
        sendChatroom(senderConnection, chatroom);
    }

    private void sendChatroom(ClientConnection senderConnection, Chatroom chatroom) {
        senderConnection.sendMessage(new ChatroomMessage(chatroom.getId(), chatroom.getOwner().getId(), chatroom.getName(), chatroom.getOwner().getHandle(), chatroom.getLatitude(), chatroom.getLongitude(), chatroom.getRadius(), chatroom.getUserCount(), (short)0));
    }

    @Override
    public void registerUser(final ClientConnection senderConnection, final String login, String password, String handle, String UUID) {
        log.debug("Registering user {}", login);

        if (login.length() == 0) {
            senderConnection.sendMessage(new RegisterRejectMessage("Invalid login"));
            return;
        }
        if (password.length() == 0) {
            senderConnection.sendMessage(new RegisterRejectMessage("Invalid password"));
            return;
        }
        if (handle.length() == 0) {
            senderConnection.sendMessage(new RegisterRejectMessage("Invalid handle"));
            return;
        }

        userRepo.registerUser(login, password, handle, UUID, new UserRepositoryCompletionHandler() {
            @Override
            public void onCompletion(final UserRepositoryActionResult result) {
                Runnable complete = new Runnable() {
                    @Override
                    public void run() {
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
                    }
                };

                runOrQueue(eventService, result, complete);
            }
        });
    }

    @Override
    public void login(final ClientConnection senderConnection, final String login, String password) {
        log.debug("Logging in user {}", login);

        if (login.length() == 0) {
            senderConnection.sendMessage(new LoginRejectMessage("Invalid login"));
            return;
        }
        if (password.length() == 0) {
            senderConnection.sendMessage(new LoginRejectMessage("Invalid password"));
            return;
        }

        userRepo.login(login, password, new UserRepositoryCompletionHandler() {
            @Override
            public void onCompletion(final UserRepositoryActionResult result) {
                Runnable complete = new Runnable() {
                    @Override
                    public void run() {
                        switch (result.getCode()) {
                            case OK:
                                final User user = result.getUser();
                                userConnectionMap.put(user, senderConnection);
                                senderConnection.setUser(user);
                                senderConnection.sendMessage(new LoginAcceptMessage(user.getId()));
                                break;
                            case ConnectionError:
                                senderConnection.sendMessage(new LoginRejectMessage("Connection error"));
                                break;
                            case InvalidUserNameOrPassword:
                            default:
                                senderConnection.sendMessage(new LoginRejectMessage(result.getMessage()));
                                break;
                        }
                    }
                };

                runOrQueue(eventService, result, complete);
            }
        });
    }

    private static void runOrQueue(EventService eventService, UserRepositoryActionResult result, Runnable complete) {
        if(result.isThreaded())
            eventService.addThreadedEvent(complete);
        else
            complete.run();
    }

    @Override
    public void quickLogin(final ClientConnection senderConnection, String handle, String UUID) {
        log.debug("Quick login user {}", handle);

        if (handle.length() == 0) {
            senderConnection.sendMessage(new LoginRejectMessage("Invalid handle"));
            return;
        }

        if (UUID.length() == 0) {
            senderConnection.sendMessage(new LoginRejectMessage("Invalid UUID"));
            return;
        }

        userRepo.registerUser(handle, handle, handle, UUID, new UserRepositoryCompletionHandler() {
                @Override
                public void onCompletion(final UserRepositoryActionResult result) {
                    Runnable complete = new Runnable() {
                        @Override
                        public void run() {
                            // assumed to be single threaded
                            switch (result.getCode()) {
                                case OK:
                                case UserAlreadyExists:
                                    User user = result.getUser();
                                    userConnectionMap.put(user, senderConnection);
                                    senderConnection.setUser(user);
                                    senderConnection.sendMessage(new LoginAcceptMessage(user.getId()));
                                    break;
                                case ConnectionError:
                                    senderConnection.sendMessage(new LoginRejectMessage("Connection error"));
                                    break;
                                case InvalidUserNameOrPassword:
                                default:
                                    senderConnection.sendMessage(new LoginRejectMessage(result.getMessage()));
                                    break;
                            }
                        }
                    };

                    runOrQueue(eventService, result, complete);
            }
        });
    }

    @Override
    public void searchChatrooms(ClientConnection sender, long latitude, long longitude, long metersFromCoords, byte onlyJoinable) {
        log.debug("Searching chatrooms {}", sender);

        Iterator<Chatroom> chatrooms = chatroomRepo.search(new ChatroomSearchCriteria(latitude, longitude, metersFromCoords, onlyJoinable));

        while(chatrooms.hasNext()) {
            sendChatroom(sender, chatrooms.next());
        }
    }

    @Override
    public void joinChatroom(ClientConnection senderConnection, User sender, Chatroom chatroom) {
        log.debug("Adding {} to {}", sender, chatroom);

        if (chatroom.containsUser(sender)) {
            senderConnection.sendMessage(new JoinChatroomRejectMessage(chatroom.getId(), sender + " is already in " + chatroom));
            return;
        }

        JoinedChatroomMessage meJoining = new JoinedChatroomMessage(sender.getId(), sender.getHandle(),
                chatroom.getId(), chatroom.getOwner().getId(), chatroom.getName(), chatroom.getOwner().getHandle(),
                chatroom.getLatitude(), chatroom.getLongitude(), chatroom.getRadius(), chatroom.getUserCount(),
                chatroom.getChatActivity());

        Iterator<User> users = chatroomRepo.getUsers(chatroom);// chatroom.getUsers();
        while(users.hasNext()) {
            // notify me about other user joining chat
            User chatMember = users.next();
            senderConnection.sendMessage(new JoinedChatroomMessage(chatMember.getId(), chatMember.getHandle(),
                    chatroom.getId(), chatroom.getOwner().getId(), chatroom.getName(), chatroom.getOwner().getHandle(),
                    chatroom.getLatitude(), chatroom.getLongitude(), chatroom.getRadius(), chatroom.getUserCount(),
                    chatroom.getChatActivity()));

            // notify other user about me joining chat
            ClientConnection chatMemberSender = userConnectionMap.get(chatMember);
            if (chatMemberSender != null) {
                chatMemberSender.sendMessage(meJoining);
            }
        }

        // Now add our user
        chatroom.addUser(sender);
        sender.addToChatroom(chatroom);

        // Give me confirmation that I've joined the chat
        senderConnection.sendMessage(meJoining);

        // send the new entrant the last N messages
        /*Iterator<ChatMessage> recentMessages = chatroom.getRecentMessages();
        while(recentMessages.hasNext()) {
            try {
                ChatMessage msg = recentMessages.next();
                MessageMessage recentMessage = new MessageMessage(msg.getId(), msg.getTimestamp(), msg.getSender().getId(), msg.getChatroom().getId(), msg.getSender().getHandle(), msg.getMessage());
                senderConnection.sendMessage(recentMessage);
            } catch (IOException e) {
                disconnect(senderConnection);
            }
        }*/
    }

    @Override
    public void leaveChatroom(ClientConnection senderConnection, User sender, Chatroom chatroom) {
        log.debug("Removing {} from {}", sender, chatroom);

        LeftChatroomMessage meLeaving = new LeftChatroomMessage(chatroom.getId(), sender.getId());

        Iterator<User> users = chatroom.getUsers();
        while(users.hasNext()) {
            User chatMember = users.next();

            // notify other user about me leaving chat
            ClientConnection chatMemberSender = userConnectionMap.get(chatMember);

            if (chatMemberSender != null)
                chatMemberSender.sendMessage(meLeaving);
        }
    }
}
