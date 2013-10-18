package com.chat.client;

import com.chat.MessageTypes;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/18/13
 * Time: 10:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChatClientListener implements Runnable {
    private final ChatClient client;
    private final DataInputStream din;

    public ChatClientListener(ChatClient client, DataInputStream stream) {
        this.client = client;
        this.din = stream;
    }

    @Override
    public void run() {
        while (true) {
            try {
                short size = din.readShort();

                byte messageType = din.readByte();
                MessageTypes types = MessageTypes.lookup(messageType);
                if (types == null) {
                    System.err.println("Unknown message type " + (int) messageType);
                }

                switch(types) {
                    case CHATROOM:
                        client.onChatroom(din.readLong(), din.readUTF(), din.readLong(), din.readUTF());
                        break;
                    case MESSAGE:
                        long msgID = din.readLong();
                        long userId = din.readLong();
                        long chatroomId = din.readLong();
                        String userName = din.readUTF();
                        String message = din.readUTF();
                        client.onMessage(userName, message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
