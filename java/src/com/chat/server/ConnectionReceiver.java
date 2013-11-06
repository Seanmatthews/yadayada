package com.chat.server;

import com.chat.BinaryStream;
import com.chat.msgs.MessageDispatcher;
import com.chat.msgs.MessageDispatcherFactory;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.ConnectMessage;
import com.chat.msgs.v1.MessageTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private final Logger log = LogManager.getLogger();

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
            log.error(stream + " Error communicating with the client: " + e.getMessage());
            e.printStackTrace();
        } catch (ValidationError e) {
            log.info(stream + " " + e.getMessage());
        } finally {
            log.debug(stream + " closing connection");
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
