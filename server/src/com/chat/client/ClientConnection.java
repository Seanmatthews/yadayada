package com.chat.client;

import com.chat.MessageTypes;
import com.chat.Utilities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientConnection {
    private final DataInputStream din;
    private final DataOutputStream dout;
    private long userId;

    public ClientConnection(DataInputStream din, DataOutputStream dout) throws IOException {
        this.din = din;
        this.dout = dout;
    }

    public void registerAndLogin(String user, String password) {
        try {
            registerNewUser(user, password);
            loginUser(user, password);
        } catch (IOException e) {
            System.out.println("Error registering and logging in");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void searchChatrooms() throws IOException {
        System.out.println("Searching for chatrooms");

        dout.writeShort(1);
        dout.writeByte(MessageTypes.SEARCH_CHATROOMS.getValue());
    }

    private void loginUser(String user, String password) throws IOException {
        System.out.println("Logging in user: " + user);
        dout.writeShort(1 + Utilities.getStringLength(user) + Utilities.getStringLength(password));
        dout.writeByte(MessageTypes.LOGIN.getValue());
        dout.writeUTF(user);
        dout.writeUTF(password);

        din.readShort(); // size
        MessageTypes msgType = MessageTypes.lookup(din.readByte());
        switch(msgType) {
            case LOGIN_ACCEPT:
                this.userId = din.readLong();
                System.out.println("Login accepted. UserId: " + userId);
                break;
            case LOGIN_REJECT:
                String msg = din.readUTF();
                System.out.println("Login rejected: " + msg);
                break;
        }
    }

    private void registerNewUser(String user, String password) throws IOException {
        System.out.println("Registering user: " + user);
        dout.writeShort(1 + Utilities.getStringLength(user) + Utilities.getStringLength(password));
        dout.writeByte(MessageTypes.REGISTER.getValue());
        dout.writeUTF(user);
        dout.writeUTF(password);

        din.readShort(); // size
        MessageTypes msgType = MessageTypes.lookup(din.readByte());
        switch(msgType) {
            case REGISTER_ACCEPT:
                this.userId = din.readLong();
                System.out.println("Registration accepted. UserId: " + user);
                break;
            case REGISTER_REJECT:
                String msg = din.readUTF();
                System.out.println("Failed to register user: " + msg);
                break;
        }
    }

    public long getUserId() {
        return userId;
    }
}
