package com.chat;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jgreco
 * Date: 10/17/13
 * Time: 8:29 AM
 * To change this template use File | Settings | File Templates.
 */
public enum MessageTypes {
    REGISTER((byte)1),
    REGISTER_ACCEPT((byte)2),
    REGISTER_REJECT((byte)3),
    QUICK_REGISTER((byte)4),

    LOGIN((byte)11),
    LOGIN_ACCEPT((byte)12),
    LOGIN_REJECT((byte)13),

    SUBMIT_MESSAGE((byte)21),
    MESSAGE((byte)22),

    SEARCH_CHATROOMS((byte)31),
    CHATROOM((byte)32),

    JOIN_CHATROOM((byte)33),
    JOIN_CHATROOM_REJECT((byte)36),
    JOINED_CHATROOM((byte)37),

    LEAVE_CHATROOM((byte)34),
    LEFT_CHATROOM((byte)38),

    CREATE_CHATROOM((byte)35);

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
