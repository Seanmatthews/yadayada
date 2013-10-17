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

    LOGIN((byte)11),
    LOGIN_ACCEPT((byte)12),
    LOGIN_REJECT((byte)13),

    SUBMIT_MESSAGE((byte)21),
    MESSAGE((byte)22),

    VIEW_CHATROOMS((byte)31),
    JOIN_CHATROOM((byte)32),
    LEAVE_CHATROOM((byte)33),
    CREATE_CHATROOM((byte)34),
    CHATROOM_CREATED((byte)35),

    ACK((byte)51),
    NAK((byte)52);

    private final byte id;

    MessageTypes(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    private static Map<Byte, MessageTypes> idToMsgMap = new HashMap<Byte, MessageTypes>();

    static {
        for (MessageTypes type : MessageTypes.values()) {
            idToMsgMap.put(type.getId(), type);
        }
    }

    public static MessageTypes lookup(byte messageType) {
        return idToMsgMap.get(messageType);
    }
 }
