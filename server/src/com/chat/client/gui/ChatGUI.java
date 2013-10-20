package com.chat.client.gui;

import com.chat.*;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientListener;
import com.chat.client.ClientConnection;
import com.chat.server.impl.InMemoryChatroomRepository;
import com.chat.server.impl.InMemoryUserRepository;
import com.chat.server.impl.SocketConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
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
    private JList chatroomList;
    private JButton searchChatroomsButton;
    private JButton leaveButton;
    private JTextField chatTextField;
    private JTextField createChatTextField;

    private final DefaultListModel<Chatroom> model = new DefaultListModel<>();
    private final Map<Component, Chatroom> componentChatroomMap = new HashMap<>();
    private final Map<Chatroom, JTextPane> chatroomComponentMap = new HashMap<>();

    private final ClientConnection connection;

    private final ChatroomRepository chatroomRepo;
    private final UserRepository userRepo;

    private User user;

    public ChatGUI(String host, int port, String user, String password) throws IOException {
        Socket socket = new Socket(host, port);
        SocketConnection socketConnection = new SocketConnection(socket);

        System.out.println("Connected to " + socket);

        connection = new ClientConnection(this, socketConnection);
        connection.registerAndLogin(user, password);

        chatroomRepo = new InMemoryChatroomRepository();
        userRepo = new InMemoryUserRepository();

        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(new ChatClientListener(this, socketConnection, chatroomRepo, userRepo));

        setupChatroomList();
    }

    private void setupChatroomList() {
        chatroomList.setModel(model);

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

                    Chatroom chatroom = (Chatroom) chatroomList.getSelectedValue();

                    JTextPane text = new JTextPane();
                    tabbedPane1.addTab(chatroom.getName(), text);

                    chatroomComponentMap.put(chatroom, text);
                    componentChatroomMap.put(text, chatroom);

                    connection.joinChatroom(user, chatroom);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.exit(0);
                }
            }
        });

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = createChatTextField.getText();

                if (text != null && text.length() > 0) {
                    try {
                        connection.createChatroom(user, text);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.exit(0);
                    }
                }
            }
        } );

        chatTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Chatroom chatroom = componentChatroomMap.get(tabbedPane1.getSelectedComponent());
                    String textToSend = chatTextField.getText();

                    try {
                        connection.sendMessage(user, chatroom, textToSend);
                        chatTextField.setText("");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.exit(0);
                    }
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
        ChatGUI chatGUI = new ChatGUI(host, port, username, password);
        chatGUI.panel1.setPreferredSize(new Dimension(600, 400));
        chatGUI.tabbedPane1.setPreferredSize(new Dimension(400, 400));

        frame.setContentPane(chatGUI.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void onChatroom(Chatroom chatroom) throws IOException {
        System.out.println("New chatroom: " + chatroom.getName() + " by " + chatroom.getOwner().getHandle());

        for(int i=0; i<model.getSize(); i++) {
            if (model.get(i).equals(chatroom)) {
                System.out.println("Duplicate chatroom: " + chatroom.getName());
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
        JTextPane chat = chatroomComponentMap.get(message.getChatroom());
        String text = chat.getText();
        chat.setText(text + message.getSender().getHandle() + ": " + message.getMessage() + "\n");
    }

    @Override
    public void sendMessage(String msg) throws IOException {
        // from the command line
    }

    @Override
    public void onJoinedChatroom(Chatroom chat, User user) {
        System.out.println(user + " has joined " + chat);
    }
}
