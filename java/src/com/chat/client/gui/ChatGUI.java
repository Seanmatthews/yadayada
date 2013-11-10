package com.chat.client.gui;

import com.chat.*;
import com.chat.client.ChatClient;
import com.chat.client.ChatClientConnection;
import com.chat.client.ChatClientDispatcher;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.*;
import com.chat.impl.InMemoryChatroomRepository;
import com.chat.impl.InMemoryUserRepository;
import com.chat.select.EventService;
import com.chat.select.impl.EventServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 5:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatGUI implements ChatClient {
    private final String userName;
    private final InMemoryUserRepository userRepo;

    private JButton createButton;
    private JPanel panel1;
    private JButton joinChatroomButton;
    private JTabbedPane tabbedPane1;
    private JList chatroomList;
    private JButton searchChatroomsButton;
    private JButton leaveButton;
    private JTextField chatTextField;
    private JTextField createChatTextField;

    private final DefaultListModel<Chatroom> chatroomModel = new DefaultListModel<>();
    private final Map<Component, Chatroom> componentChatroomMap = new HashMap<>();
    private final Map<Chatroom, JTextPane> chatroomTextMap = new HashMap<>();
    private final Map<Chatroom, JList<User>> chatroomListMap = new HashMap<>();
    private final Map<Chatroom, JPanel> chatroomPanelMap = new HashMap<>();

    private final ChatClientConnection connection;

    private User user;

    public ChatGUI(String host, int port, String userName, String password) throws IOException, ValidationError {
        EventService eventService = new EventServiceImpl();

        ChatroomRepository chatroomRepo = new InMemoryChatroomRepository();
        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        ChatClientDispatcher dispatcher = new ChatClientDispatcher(this, chatroomRepo, userRepo);

        connection = new ChatClientConnection("CLIENT", eventService, host, port, dispatcher, userName, password);

        this.userName = userName;
        this.userRepo = userRepo;

        setupActions();

        eventService.run();
    }

    private void setupActions() {
        chatroomList.setModel(chatroomModel);

        leaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            Chatroom chatroom = (Chatroom) chatroomList.getSelectedValue();
            connection.sendMessage(new LeaveChatroomMessage(user.getId(), chatroom.getId()));
            }
        });

        searchChatroomsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            connection.sendMessage(new SearchChatroomsMessage(0, 0));
            }
        });

        joinChatroomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            if (user == null)
                return;

            Chatroom chatroom = (Chatroom) chatroomList.getSelectedValue();
            connection.sendMessage(new JoinChatroomMessage(user.getId(), chatroom.getId(), 0, 0));
            }
        });

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            String text = createChatTextField.getText();

            if (text != null && text.length() > 0) {
                connection.sendMessage(new CreateChatroomMessage(user.getId(), text, 0, 0, 0));
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

                    connection.sendMessage(new SubmitMessageMessage(user.getId(), chatroom.getId(), textToSend));
                    chatTextField.setText("");
                }
            }
        });
    }

    @Override
    public void onLoginAccept(long userId) {
        user = new User(userId, userName, userRepo);
        userRepo.addUser(user);
    }

    @Override
    public void onLoginReject(String reason) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRegisterAccept(long userId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onRegisterReject(String reason) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static void main(String[] args) throws IOException, InterruptedException, ValidationError {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String username = args[2];
        String password = args[3];

        JFrame frame = new JFrame("Chatter");
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

        for(int i=0; i< chatroomModel.getSize(); i++) {
            if (chatroomModel.get(i).equals(chatroom)) {
                System.out.println("Duplicate chatroom: " + chatroom.getName());
                return;
            }
        }

        chatroomModel.addElement(chatroom);
    }

    @Override
    public void onMessage(ChatMessage message) {
        JTextPane chat = chatroomTextMap.get(message.getChatroom());
        String text = chat.getText();
        chat.setText(text + message.getSender().getHandle() + ": " + message.getMessage() + "\n");
    }

    @Override
    public void onJoinedChatroom(Chatroom chatroom, User user) {
        System.out.println(user + " has joined " + chatroom);

        JList<User> userJList = chatroomListMap.get(chatroom);

        if (userJList == null) {
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(1, 2, 15, 15));

            JTextPane text = new JTextPane();
            panel.add(text);

            userJList = new JList<>();
            userJList.setModel(new DefaultListModel<User>());
            panel.add(userJList);

            tabbedPane1.addTab(chatroom.getName(), panel);

            chatroomPanelMap.put(chatroom, panel);
            chatroomTextMap.put(chatroom, text);
            chatroomListMap.put(chatroom, userJList);
            componentChatroomMap.put(panel, chatroom);
        }

        if (!userJList.getSelectedValuesList().contains(user)) {
            ((DefaultListModel<User>)userJList.getModel()).addElement(user);
        }
    }

    @Override
    public void onJoinedChatroomReject(String reason) {
        System.err.println("Error entering chatroom: " + reason);
    }

    @Override
    public void onConnectAccept(int apiVersion, long globalChatId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onConnectReject(String reason) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onLeftChatroom(Chatroom chatroom, User user) {
        System.out.println(user + " has left " + chatroom);

        JList<User> userJList = chatroomListMap.get(chatroom);

        if (userJList != null) {
            ((DefaultListModel<User>)userJList.getModel()).removeElement(user);
        }

        if (user.equals(this.user)) {
            JPanel panel = chatroomPanelMap.get(chatroom);
            if (panel != null) {
                tabbedPane1.remove(panel);

                chatroomListMap.remove(chatroom);
                chatroomPanelMap.remove(chatroom);
                chatroomTextMap.remove(chatroom);
            }
        }
    }
}
