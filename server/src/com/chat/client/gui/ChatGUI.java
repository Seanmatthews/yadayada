package com.chat.client.gui;

import com.chat.*;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientListener;
import com.chat.client.ClientConnection;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
    private JButton createButton;
    private JPanel panel1;
    private JButton joinChatroomButton;
    private JTabbedPane tabbedPane1;
    private JList chatList;
    private JButton searchChatroomsButton;

    private final DefaultListModel<Chatroom> model;

    private final Socket socket;
    private final DataOutputStream dout;
    private final ClientConnection connection;

    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;

    private User user;

    public ChatGUI(String host, int port, String user, String password) throws IOException {
        socket = new Socket(host, port);

        System.out.println("Connected to " + socket);

        DataInputStream din = new DataInputStream(socket.getInputStream());
        dout = new DataOutputStream(socket.getOutputStream());

        connection = new ClientConnection(this, din, dout);
        connection.registerAndLogin(user, password);

        model = new DefaultListModel<Chatroom>();

        chatroomRepo = new InMemoryChatroomRepository();
        userRepo = new InMemoryUserRepository();

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatClientListener(this, din, chatroomRepo, userRepo));

        setupChatroomList();
    }

    private void setupChatroomList() {
        chatList.setModel(model);
        chatList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Chatroom chatroom = model.get(e.getFirstIndex());
            }
        });

        searchChatroomsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    connection.searchChatrooms();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.exit(0);
                }
            }
        });

        joinChatroomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (user == null)
                        return;

                    Chatroom chatroom = (Chatroom) chatList.getSelectedValue();
                    connection.joinChatroom(user, chatroom);

                    JTextPane text = new JTextPane();
                    tabbedPane1.addTab(chatroom.name, text);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.exit(0);
                }
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
    public void onChatroom(Chatroom chatroom) throws IOException {
        System.out.println("New chatroom: " + chatroom.name + " by " + chatroom.owner.login);

        for(int i=0; i<model.getSize(); i++) {
            if (model.get(i).equals(chatroom)) {
                System.out.println("Duplicate chatroom: " + chatroom.name);
                return;
            }
        }

        model.addElement(chatroom);
    }

    @Override
    public void onUserLoggedIn(User user) {
        this.user = user;
    }

    @Override
    public void onMessage(Message message) {
        JTextPane chat = (JTextPane) tabbedPane1.getComponentAt(0);
        String text = chat.getText();
        chat.setText(text + message.sender.login + ": " + message.message + "\n");
    }

    @Override
    public void sendMessage(String msg) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
