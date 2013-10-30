package com.chat.server;

import com.chat.BinaryStream;
import com.chat.ChatroomRepository;
import com.chat.UserRepository;
import com.chat.msgs.V1Dispatcher;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.ClientConnectionImpl;
import com.chat.msgs.v1.ConnectMessage;
import com.chat.msgs.v1.MessageTypes;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/29/13
 * Time: 6:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionReceiver implements Runnable {
    private final BinaryStream stream;
    private final ChatServer server;
    private final UserRepository userRepo;
    private final ChatroomRepository chatroomRepo;

    public ConnectionReceiver(ChatServer server, BinaryStream stream, UserRepository userRepo, ChatroomRepository chatroomRepo) {
        this.server = server;
        this.userRepo = userRepo;
        this.chatroomRepo = chatroomRepo;
        this.stream = stream;
    }

    @Override
    public void run() {
        try {
            MessageTypes type = recvMsgType();

            if (type == MessageTypes.Connect) {
                ConnectMessage cMsg = recvConnect();
                int apiVersion = cMsg.getAPIVersion();
                String uuid = cMsg.getUUID();

                if (apiVersion == 1) {
                    new V1Dispatcher(server, new ClientConnectionImpl(stream, uuid, apiVersion), userRepo, chatroomRepo).run();
                }
                else {
                    throw new ValidationError("Unsupported API Version: " + apiVersion);
                }
            }
            else {
                throw new ValidationError("Connect message is the first message required from the client");
            }
        } catch (IOException e) {
            System.err.println("Error communicating with the client: " + e.getMessage());
            e.printStackTrace();
        } catch (ValidationError validationError) {
            System.err.println(validationError.getMessage());
        } finally {
            System.out.println("Closing connection");
            stream.close();
        }
    }

    public MessageTypes recvMsgType() throws IOException, ValidationError {
        stream.startReading();
        byte msgTypeByte = stream.readByte();
        MessageTypes msgType = MessageTypes.lookup(msgTypeByte);

        if (msgType == null)
            throw new ValidationError("Unknown message type: " + (int)msgTypeByte);

        return msgType;
    }

    public ConnectMessage recvConnect() throws IOException {
        int APIVersion = stream.readInt();
        String UUID = stream.readString();
        stream.finishReading();

        return new ConnectMessage(APIVersion, UUID);
    }
}
