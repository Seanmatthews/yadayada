package com.chat.map;

import com.chat.*;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientConnection;
import com.chat.client.ChatClientDispatcher;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;
import com.chat.msgs.ValidationError;
import com.chat.select.EventService;
import com.chat.select.impl.EventServiceImpl;
import com.chat.util.NanoHTTPD;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/29/13
 * Time: 9:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class MapServer extends NanoHTTPD implements ChatClient {
    private final ChatClientConnection connection;
    private User user;
    private final FileInputStream mapHtmlFile;

    private final InMemoryChatroomRepository chatroomRepo;
    private final InMemoryUserRepository userRepo;
    private final Map<User, ChatMessage> lastMessage = new HashMap<>();

    public MapServer(int webPort, String host, int port, String username, String password) throws IOException, ValidationError {
        super(webPort);

        mapHtmlFile = new FileInputStream("/tmp/maps.html");

        EventService eventService = new EventServiceImpl();

        chatroomRepo = new InMemoryChatroomRepository();
        userRepo = new InMemoryUserRepository();
        ChatClientDispatcher dispatcher = new ChatClientDispatcher(this, chatroomRepo, userRepo);

        connection = new ChatClientConnection("CLIENT", eventService, host, port, dispatcher, username, password);

        //long userId = ChatClientUtilities.initialConnect(connection, userName, password);
        //user = new User(userId, userName, userRepo);
        //userRepo.addUser(user);

        eventService.run();
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();

        if (method.equals(Method.GET)) {
            String uri = session.getUri();
            if (uri.contains("map.html")) {
                return new Response(Response.Status.OK, MIME_HTML, mapHtmlFile);
            }
            else if(uri.contains("chatrooms.json")) {
                StringBuilder bld = new StringBuilder();
                bld.append("[");

                Iterator<Chatroom> iterator = chatroomRepo.iterator();
                while (iterator.hasNext()) {
                    Chatroom chatroom = iterator.next();
                    bld.append("{");
                    bld.append("\"name\":\"").append(chatroom.getName()).append("\"");
                    bld.append("\"owner\":\"").append(chatroom.getOwner().getHandle()).append("\"");
                    bld.append("}");

                    if (iterator.hasNext())
                        bld.append(",");
                }

                bld.append("]");
                return new Response(Response.Status.OK, "application/json", bld.toString());
            }
        }

        return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found");
    }

    public static void main(String[] args) throws IOException, ValidationError {
        int webPort = Integer.parseInt(args[0]);
        String host = args[1];
        int port = Integer.parseInt(args[2]);
        String username = args[3];
        String password = args[4];
        new MapServer(webPort, host, port, username, password).start();
    }

    @Override
    public void onChatroom(Chatroom chatroom) throws IOException {
        // nothing, already have repo
    }

    @Override
    public void onMessage(ChatMessage message) {
        lastMessage.put(message.getSender(), message);
    }

    @Override
    public void onJoinedChatroom(Chatroom chat, User user) {
        chat.addUser(user);
    }

    @Override
    public void onLeftChatroom(Chatroom chatroom, User user) {
        chatroom.removeUser(user);
    }

    @Override
    public void onJoinedChatroomReject(String reason) {
        System.err.println("Error entering chatroom: " + reason);
    }

    @Override
    public void onLoginAccept(long userId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
