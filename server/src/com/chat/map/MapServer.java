package com.chat.map;

import com.chat.*;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientDispatcher;
import com.chat.client.ChatClientUtilities;
import com.chat.impl.DataStream;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;
import com.chat.msgs.v1.ServerConnectionImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/29/13
 * Time: 9:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class MapServer extends NanoHTTPD implements ChatClient {
    private final ServerConnectionImpl connection;
    private final User user;
    private final FileInputStream mapHtmlFile;

    private final InMemoryChatroomRepository chatroomRepo;
    private final InMemoryUserRepository userRepo;
    private final Map<User, Message> lastMessage = new HashMap<>();

    public MapServer(int webPort, String host, int port, String username, String password) throws IOException {
        super(webPort);

        mapHtmlFile = new FileInputStream("/tmp/maps.html");

        Socket socket = new Socket(host, port);
        BinaryStream dout = new DataStream(socket);

        System.out.println("Connected to " + socket);

        connection = new ServerConnectionImpl(dout);

        long userId = ChatClientUtilities.initialConnect(connection, username, password);
        user = new User(userId, username);

        chatroomRepo = new InMemoryChatroomRepository();
        userRepo = new InMemoryUserRepository();
        userRepo.addUser(user);

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatClientDispatcher(this, dout, chatroomRepo, userRepo));

        System.out.println("Connected!");
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

    public static void main(String[] args) throws IOException {
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
    public void onMessage(Message message) {
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
}
