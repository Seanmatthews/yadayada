package com.chat.msgs.v1;

import java.util.HashMap;
import java.util.Map;

public enum MessageTypes {
    Register((byte)1),
    Login((byte)11),
    Connect((byte)16),
    SubmitMessage((byte)21),
    SearchChatrooms((byte)31),
    JoinChatroom((byte)33),
    LeaveChatroom((byte)34),
    CreateChatroom((byte)35),
    RegisterAccept((byte)2),
    RegisterReject((byte)3),
    LoginAccept((byte)12),
    LoginReject((byte)13),
    ConnectAccept((byte)17),
    ConnectReject((byte)18),
    Message((byte)22),
    Chatroom((byte)32),
    JoinChatroomReject((byte)36),
    JoinedChatroom((byte)37),
    LeftChatroom((byte)38),
    CreateChatroomReject((byte)39),
    ;

    private final byte value;

    MessageTypes(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    private static Map<Byte, MessageTypes> idToMsgMap = new HashMap<Byte, MessageTypes>();

    static {
        for (MessageTypes type : MessageTypes.values()) {
            idToMsgMap.put(type.getValue(), type);
        }
    }

    public static MessageTypes lookup(byte messageType) {
        return idToMsgMap.get(messageType);
    }
}


