package com.chat.client.gui;

import com.chat.Chatroom;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientListener;
import com.chat.client.ClientConnection;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 5:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatGUI implements ChatClient {
    private final Socket socket;
    private final DataOutputStream dout;
    private final ClientConnection connection;

    DefaultListModel<Chatroom> model = new DefaultListModel<Chatroom>();
    private final List<Chatroom> chatrooms = new ArrayList<Chatroom>();

    private JButton createChatButton;
    private JPanel panel1;
    private JButton joinChatButton;
    private JTabbedPane tabbedPane1;
    private JList chatList;

    public ChatGUI(String host, int port, String user, String password) throws IOException {
        socket = new Socket(host, port);

        System.out.println("Connected to " + socket);

        DataInputStream din = new DataInputStream(socket.getInputStream());
        dout = new DataOutputStream(socket.getOutputStream());

        connection = new ClientConnection(din, dout);
        connection.registerAndLogin(user, password);
        connection.searchChatrooms();

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatClientListener(this, din));

        chatList.setModel(model);
        chatList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Chatroom chatroom = model.get(e.getFirstIndex());
            }
        });
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String username = args[2];
        String password = args[3];

        JFrame frame = new JFrame("MyForm");
        frame.setContentPane(new ChatGUI(host, port, username, password).panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void onChatroom(long chatroomId, String chatroomName, long ownerUserId, String ownerName) throws IOException {
        System.out.println("New chatroom: " + chatroomName + " by " + ownerName);

        Chatroom chatroom = new Chatroom();
        chatroom.id = chatroomId;
        chatroom.name = chatroomName;
        model.addElement(chatroom);
    }

    @Override
    public void onMessage(String userName, String message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void sendMessage(String msg) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
