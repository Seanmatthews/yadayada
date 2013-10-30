package com.chat.server;

import com.chat.BinaryStream;
import com.chat.msgs.MessageDispatcher;
import com.chat.msgs.MessageDispatcherFactory;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.ConnectMessage;
import com.chat.msgs.v1.MessageTypes;

import java.io.IOException;

import static com.chat.msgs.Utilities.getStrLen;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/29/13
 * Time: 6:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionReceiver implements Runnable {
    private final BinaryStream stream;
    private final MessageDispatcherFactory factory;

    public ConnectionReceiver(MessageDispatcherFactory factory, BinaryStream stream) {
        this.factory = factory;
        this.stream = stream;
    }

    @Override
    public void run() {
        try {
            MessageTypes type = recvMsgType();

            if (type == MessageTypes.Connect) {
                ConnectMessage msg = recvConnect();
                MessageDispatcher dispatcher = factory.getDispatcher(msg.getAPIVersion(), stream, msg.getUUID());
                dispatcher.run();
            }
            else {
                throw new ValidationError(stream + " Connect message is the first message required from the client");
            }
        } catch (IOException e) {
            System.err.println(stream + " Error communicating with the client: " + e.getMessage());
            e.printStackTrace();
        } catch (ValidationError e) {
            System.err.println(stream + " " + e.getMessage());
        } finally {
            System.out.println(stream + " Closing connection");
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

    public void rejectConnect(String message) throws IOException {
        stream.startWriting(1 + getStrLen(message));
        stream.writeByte(MessageTypes.ConnectReject.getValue());
        stream.writeString(message);
        stream.finishWriting();
    }

}
