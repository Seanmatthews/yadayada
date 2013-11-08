package com.chat.client;

import com.chat.ClientConnection;
import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.*;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/23/13
 * Time: 7:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatClientUtilities {
    public static long initialConnect(ClientConnection connection, String user, String password) throws IOException, ValidationError {
        /*connection.sendMessage(new ConnectMessage(connection.getAPIVersion(), connection.getUUID()), true);

        MessageTypes types = MessageTypes.lookup(connection.readByte());
        new ConnectAcceptMessage(connection);

        connection.sendMessage(new RegisterMessage(user, password, user), true);

        types = MessageTypes.lookup(connection.readByte());
        if (types == MessageTypes.RegisterAccept)
            new RegisterAcceptMessage(connection);
        else
            new RegisterRejectMessage(connection);

        connection.sendMessage(new LoginMessage(user, password), true);

        long userId = 0;

        types = MessageTypes.lookup(connection.readByte());
        if (types == MessageTypes.LoginAccept)
            userId = new LoginAcceptMessage(connection).getUserId();
        else
            new LoginRejectMessage(connection);

        return userId;*/

        return 0;
    }
}
