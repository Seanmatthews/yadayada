package com.chat.client;

import com.chat.msgs.ValidationError;
import com.chat.msgs.v1.*;

import java.io.IOException;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/23/13
 * Time: 7:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatClientUtilities {
    public static long initialConnect(ServerConnection connection, String user, String password) throws IOException, ValidationError {
        connection.sendConnect(new ConnectMessage(connection.getAPIVersion(), connection.getUUID()));
        connection.recvMsgType();
        connection.recvConnectAccept();

        connection.sendRegister(new RegisterMessage(user, password, user));

        MessageTypes types = connection.recvMsgType();
        if (types == MessageTypes.RegisterAccept)
            connection.recvRegisterAccept();
        else
            connection.recvRegisterAccept();

        connection.sendLogin(new LoginMessage(user, password));

        long userId = 0;

        types = connection.recvMsgType();
        if (types == MessageTypes.LoginAccept)
            userId = connection.recvRegisterAccept().getUserId();
        else
            connection.recvRegisterAccept();

        connection.sendSearchChatrooms(new SearchChatroomsMessage(0, 0));

        return userId;
    }
}
