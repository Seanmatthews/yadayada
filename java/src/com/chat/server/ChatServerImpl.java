package com.chat.server;

import com.chat.*;
import com.chat.msgs.v1_4_0.*;
import com.chat.select.EventService;
import com.chat.server.cluster.ChatroomCluster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.chat.UserRepository.UserRepositoryActionResult;
import static com.chat.UserRepository.UserRepositoryCompletionHandler;

import com.relayrides.pushy.apns.*;
import com.relayrides.pushy.apns.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatServerImpl implements ChatServer {
    private final Logger log = LogManager.getLogger();
    private PushManager<SimpleApnsPushNotification> pushManager;

    private final EventService eventService;
    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;
    private final MessageRepository messageRepo;
    private final Map<User, ClientConnection> userConnectionMap = new HashMap<>();

    public ChatServerImpl(EventService eventService, UserRepository userRepo, ChatroomRepository chatroomRepo,
                          MessageRepository messageRepo, PushManager<SimpleApnsPushNotification> pushManager) {
        this.eventService = eventService;
        this.chatroomRepo = chatroomRepo;
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
        this.pushManager = pushManager;
    }

    private void sendMessageAsNotification(User toUser, MessageMessage mm) throws InterruptedException {
        HashMap<String,Object> messageFields = new HashMap<String,Object>();
        messageFields.put("message", mm.getMessage());
        messageFields.put("messageTimestamp", mm.getMessageTimestamp());
        messageFields.put("senderHandle", mm.getSenderHandle());
        messageFields.put("chatroomId", mm.getChatroomId());
        messageFields.put("messageId", mm.getMessageId());
        messageFields.put("senderId", mm.getSenderId());

        final byte[] token = TokenUtil.tokenStringToByteArray(toUser.getDeviceToken());
        final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
        payloadBuilder.setSoundFileName("");
        payloadBuilder.setContentAvailable(true);
        payloadBuilder.addCustomProperty("messageType", MessageTypes.Message.getValue());
        payloadBuilder.addCustomProperty("msg", messageFields);
        final String payload = payloadBuilder.buildWithDefaultMaximumLength();
        pushManager.getQueue().put(new SimpleApnsPushNotification(token, payload));
    }

    @Override
    public void disconnect(ClientConnection sender) {
        User user = sender.getUser();

        log.debug("Disconnect {}", sender);

        if (user != null) {
            // Also, don't set this user to null, but do remove their connection
            // from the user connection map below.
//            sender.setUser(null);

            // Remove the user's specific connection information
            //userConnectionMap.remove(user);

            // When a user disconnects, it means their connection was interrupted,
            // not necessarily that they desire to disconnect.
            // So, mark the user as disconnected, but do not remove the from chatrooms yet.
            log.debug("{} {} has disconnected", user, sender);
            user.setConnected(false);

//            log.debug("Removing connection {} {}", user, sender);
//            Iterator<Chatroom> chatrooms = user.getChatrooms();
//            while(chatrooms != null && chatrooms.hasNext()) {
//                Chatroom chatroom = chatrooms.next();
//
//                // Remove our user
//                chatroom.removeUser(user);
//                chatrooms.remove();
//                //user.removeFromChatroom(chatroom);
//
//                Iterator<User> users = chatroom.getUsers();
//
//                while(users.hasNext()) {
//                    User chatroomUser = users.next();
//                    ClientConnection chatroomUserConnection = userConnectionMap.get(chatroomUser);
//
//                    if (chatroomUserConnection != null)
//                        leaveChatroom(chatroomUserConnection, user, chatroom);
//                }
//            }
        }
        else {
            log.debug("User is null. Removing connection {}", sender);
        }
    }

    @Override
    public void connect(ClientConnection sender, int apiVersion, String uuid) {
        sender.sendMessage(new ConnectAcceptMessage(apiVersion, 1, "", "", (short) 30));
    }

    @Override
    public void streamReset(ClientConnection senderConnection, User user, Byte appAwake) {
        // If the client is in a background mode, we want to send it push notifications,
        // and not regular messages over this connection
//        if (1 == appAwake) {
            userConnectionMap.put(user, senderConnection);
            senderConnection.setUser(user);
            user.setConnected(true);
//        }
    }

    @Override
    public void newMessage(ClientConnection senderConnection, User sender, Chatroom chatroom, String message) {
        if (message.length() == 0 || message.length() > ChatMessage.MAX_LENGTH) {
            senderConnection.sendMessage(new SubmitMessageRejectMessage(sender.getId(), chatroom.getId(),
                    "Invalid message length: " + message.length()));
            return;
        }

        if (!chatroomRepo.containsUser(chatroom, sender)) {
            log.debug("Chat sender {} not in chatroom {}", sender, chatroom);
            senderConnection.sendMessage(new SubmitMessageRejectMessage(sender.getId(), chatroom.getId(),
                    "Not in chatroom: " + chatroom.getName()));
            return;
        }

        ChatMessage msg = messageRepo.create(chatroom, sender, message);
        ChatroomCluster cluster = chatroom.addMessage(msg);

        MessageMessage msgToSend = new MessageMessage(msg.getId(), msg.getTimestamp(),
                msg.getSender().getId(), msg.getChatroom().getId(), msg.getSender().getHandle(), msg.getMessage());

        Iterator<User> chatUsers = cluster.getUsers();
        while (chatUsers.hasNext()) {
            User user = chatUsers.next();
            log.debug("Message to user {} {} {}", user, user.getUUID(), user.getDeviceToken());

            ClientConnection connection = userConnectionMap.get(user);

            if (connection != null) {
//                // TEST
//                for (int i=0; i<10; ++i) {
//                    connection.sendMessage(msgToSend);
//                }

                if (!user.isConnected()) {
                    try {
                        // Send APNS
                        log.debug("Sending APNS to user {} {}", user, user.getId());
                        sendMessageAsNotification(user, msgToSend);
                    }
                    catch (InterruptedException e) {
                        log.debug("Pushy exception {}", e.getMessage());
                    }
                }
                else {
                    log.debug("Sending message to user {} {}", user, user.getId());
                    connection.sendMessage(msgToSend);
                }
            }
        }
    }

    @Override
    public void createChatroom(ClientConnection senderConnection, User sender, String name, long latitude,
                               long longitude, long radius, boolean isPrivate) {
        log.debug("Creating chatroom {} by {}", name, sender);

        Chatroom chatroom = chatroomRepo.createChatroom(sender, name, latitude, longitude, radius, isPrivate);
        sendChatroom(senderConnection, chatroom);
    }

    private void sendChatroom(ClientConnection senderConnection, Chatroom chatroom) {

        senderConnection.sendMessage(
                new ChatroomMessage(
                chatroom.getId(),
                chatroom.getOwner().getId(),
                chatroom.getName(),
                chatroom.getOwner().getHandle(),
                chatroom.getLatitude(),
                chatroom.getLongitude(),
                chatroom.getRadius(),
                chatroom.getUserCount(),
                chatroom.getChatActivity(),
                chatroom.isPrivate() ? (byte)1 : (byte)0));
    }

    @Override
    public void registerUser(final ClientConnection senderConnection, final String login, String password,
                             String handle, String UUID, long phoneNumber, String deviceTokenString) {
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

        userRepo.registerUser(login, password, handle, UUID, phoneNumber, deviceTokenString,
                new UserRepositoryCompletionHandler() {
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
    public void quickLogin(final ClientConnection senderConnection, String handle, String UUID,
                           long phoneNumber, String deviceTokenString) {
        log.debug("Quick login user {} {}", handle, phoneNumber);

        if (handle.length() == 0) {
            senderConnection.sendMessage(new LoginRejectMessage("Invalid handle"));
            return;
        }

        if (UUID.length() == 0) {
            senderConnection.sendMessage(new LoginRejectMessage("Invalid UUID"));
            return;
        }

        userRepo.registerUser(handle, handle, handle, UUID, phoneNumber, deviceTokenString,
                new UserRepositoryCompletionHandler() {
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

                                    // Need to check userConnectionMap for duplicate UUID--
                                    // this weeds out old users that were created by this device.
                                    Iterator it = userConnectionMap.entrySet().iterator();
                                    while (it.hasNext()) {
                                        Map.Entry pairs = (Map.Entry)it.next();
                                        User u = (User)pairs.getKey();
                                        if (u.getDeviceToken().equalsIgnoreCase(user.getDeviceToken())) {
                                            terminate((ClientConnection)pairs.getValue());
                                        }
                                    }

                                    userConnectionMap.put(user, senderConnection);
                                    senderConnection.setUser(user);
                                    user.setConnected(true);
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
            Chatroom c = chatrooms.next();
            if (!c.isPrivate()) {
                sendChatroom(sender, c);
            }
        }
    }

    @Override
    public void joinChatroom(ClientConnection senderConnection, User sender, Chatroom chatroom) {
        log.debug("Adding {} {} to {}", sender, sender.getId(), chatroom);

        JoinedChatroomMessage meJoining = new JoinedChatroomMessage(sender.getId(), sender.getHandle(),
                chatroom.getId(), chatroom.getOwner().getId(), chatroom.getName(), chatroom.getOwner().getHandle(),
                chatroom.getLatitude(), chatroom.getLongitude(), chatroom.getRadius(), chatroom.getUserCount(),
                chatroom.getChatActivity());

        if (!chatroom.containsUser(sender)) {
            if (chatroom.usernameInUse(sender)) {
                senderConnection.sendMessage(new JoinChatroomRejectMessage(chatroom.getId(),
                        sender + " handle already used in " + chatroom));
                log.debug("User {} already in chatroom {}", sender, chatroom);
                return;
            }

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
        }

        // TODO should we send confirmation if the user is already joined?
        // TODO Maybe yes? This makes it idempotent and predictable
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

        LeftChatroomMessage meLeaving = new LeftChatroomMessage(chatroom.getId(), sender.getId(), sender.getHandle());

        Iterator<User> users = chatroom.getUsers();
        while(users.hasNext()) {
            User chatMember = users.next();

            // notify other user about me leaving chat
            ClientConnection chatMemberSender = userConnectionMap.get(chatMember);

            if (chatMemberSender != null)
                chatMemberSender.sendMessage(meLeaving);
        }
    }

    @Override
    public void inviteUser(ClientConnection sender, long chatroomId, long recipientId, long recipientPhone) throws ExecutionException, InterruptedException {
        log.debug("Inviting user {} to chatroom {}", recipientPhone, chatroomId);

        // NOTE: Note using phone number since 6/12/14

        // Does the recipient exist in the system?
        User user = null;
        synchronized (userRepo) {
//            user = userRepo.getFromPhone(recipientPhone, null).get().getUser();
            user = userRepo.get(recipientId, null).get().getUser();
        }

        if (user == null) {
            log.debug("Invited user doesn't exist");
            sender.sendMessage(new InviteUserRejectMessage("User does not exist"));
        }
        else {
            // Send response to sender
            Chatroom chat = chatroomRepo.get(chatroomId);
            sender.sendMessage(new InviteUserSuccessMessage(user.getId(), user.getHandle(), chat.getName()));

            // Send invite to recipient
            ClientConnection recipientConnection = userConnectionMap.get(user);
            if (recipientConnection != null) {
                log.debug("Sending invite");
                recipientConnection.sendMessage(new InviteUserMessage(sender.getUser().getId(),
                                                                      sender.getUser().getHandle(),
                                                                      user.getId(),
                                                                      chatroomId,
                                                                      chat.getName(),
                                                                      chat.getLatitude(),
                                                                      chat.getLongitude(),
                                                                      chat.getRadius(),
                                                                      user.getPhoneNumber()));
            }
        }
    }

    @Override
    public void heartbeat(ClientConnection sender, long timestamp, long latitude, long longitude) {
        log.debug("Received heartbeat");
        log.debug("Received heartbeat from user {}", sender.getUser().getId());

        User user = sender.getUser();
        user.setLatitude(latitude);
        user.setLongitude(longitude);
        user.setLastHeartbeat(timestamp);
    }

    @Override
    public void terminate(ClientConnection sender) {

        User user = sender.getUser();

        if (user != null) {
            // Remove user from all chatrooms
            log.debug("Terminating user {} {}", user, sender);
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

            // Remove user connections
            userConnectionMap.remove(user);
        }
    }

    @Override
    public void changeUserHandle(ClientConnection sender, long userId, String oldHandle, String handle) {
        User user = sender.getUser();
//        User user = userRepo.get(userId, null).get().getUser();

        if (user != null) {

            // Reject if the new handle exists in one of a user's joined chatrooms
            Iterator it = user.getChatrooms();
            while (it.hasNext()) {
                Chatroom chatroom = (Chatroom)it.next();
                if (chatroom.usernameInUse(handle)) {
                    sender.sendMessage(new ChangeHandleRejectMessage(handle, oldHandle,
                            "Handle in use in a joined chatroom"));
                    return;
                }
            }

            userRepo.changeLogin(user, handle);
            user.setHandle(handle);
            sender.sendMessage(new ChangeHandleAcceptMessage(handle));
        }

    }

//    // TODO download java 1.8
//    interface Predicate<User> {
//        boolean test(User u);
//    }
//
//    Iterator<User> searchOnQuery(Collection<User> allUsers, Predicate<User> p) {
//        List<User> matches = new Vector<User>();
//        for (User u : allUsers) {
//            if (p.test(u)) {
//                matches.add(u);
//            }
//        }
//    }


    // WARNING: This function creates a lot of message spam. Also,
    // it may tie up the server unnecessarily.
    // Can we do this in a backgroun thread, or is ClientConnection not thread safe?
    @Override
    public void searchUsers(ClientConnection sender, String query) {
        User user = sender.getUser();
        if (user != null) {

            // TODO fix this search
//            Iterator it = searchOnQuery(userRepo.getAllUsers(),
//                           u -> u.getHandle().toLowerCase().startsWith(query.toLowerCase()) &&
//                                u.getHandle().length() >= query.length() &&
//                                u.getHandle().length() <= query.length() + query.length());

            Iterator it = userRepo.getAllUsers().iterator();
            while (it.hasNext()) {
                User uu = (User)it.next();
                log.debug("MAtching user {}", uu);
                if (uu.getHandle().toLowerCase().startsWith(query.toLowerCase()) &&
                    uu.getHandle().length() >= query.length() &&
                    uu.getHandle().length() <= query.length() + query.length()) {

                    UserInfoMessage uim = new UserInfoMessage(uu.getId(), uu.getHandle(), uu.getUUID());
                    sender.sendMessage(uim);
                }
            }
        }
    }
}